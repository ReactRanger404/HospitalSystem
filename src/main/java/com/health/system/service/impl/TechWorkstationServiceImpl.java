package com.health.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.system.common.exception.BusinessException;
import com.health.system.entity.ExamRequest;
import com.health.system.entity.ExamResult;
import com.health.system.mapper.ExamRequestMapper;
import com.health.system.mapper.ExamResultMapper;
import com.health.system.service.MessageProducerService;
import com.health.system.service.TechWorkstationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 医技工作站服务实现
 *
 * @author health-system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TechWorkstationServiceImpl implements TechWorkstationService {

    private final ExamRequestMapper examRequestMapper;
    private final ExamResultMapper examResultMapper;
    private final MessageProducerService messageProducer;

    @Override
    public IPage<ExamRequest> getPendingExams(String examCategory, int page, int size) {
        LambdaQueryWrapper<ExamRequest> wrapper = new LambdaQueryWrapper<ExamRequest>()
                .eq(ExamRequest::getStatus, "pending")
                .orderByDesc(ExamRequest::getUrgency)
                .orderByAsc(ExamRequest::getCreatedAt);
        if (examCategory != null) {
            wrapper.eq(ExamRequest::getExamCategory, examCategory);
        }
        return examRequestMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public ExamRequest startExam(Long examRequestId, Long technicianId) {
        ExamRequest exam = examRequestMapper.selectById(examRequestId);
        if (exam == null) throw new BusinessException("申请单不存在");
        if (!"pending".equals(exam.getStatus())) {
            throw new BusinessException("申请单状态不允许接诊: " + exam.getStatus());
        }
        exam.setStatus("in_progress");
        examRequestMapper.updateById(exam);
        log.info("登记接诊: examRequestId={}, technicianId={}", examRequestId, technicianId);
        return exam;
    }

    @Override
    @Transactional
    public ExamResult saveResult(Long examRequestId, Long technicianId, ExamResult data) {
        ExamResult existing = examResultMapper.selectOne(
                new LambdaQueryWrapper<ExamResult>().eq(ExamResult::getExamRequestId, examRequestId));

        if (existing != null) {
            // 更新已有结果
            if (data.getResultDescription() != null) existing.setResultDescription(data.getResultDescription());
            if (data.getConclusion() != null) existing.setConclusion(data.getConclusion());
            if (data.getAbnormalFlags() != null) existing.setAbnormalFlags(data.getAbnormalFlags());
            if (data.getImageUrls() != null) existing.setImageUrls(data.getImageUrls());
            if (data.getEquipment() != null) existing.setEquipment(data.getEquipment());
            examResultMapper.updateById(existing);
            log.info("更新检查结果: examResultId={}", existing.getId());
            return existing;
        } else {
            // 新建结果
            ExamResult result = new ExamResult();
            result.setExamRequestId(examRequestId);
            result.setTechnicianId(technicianId);
            result.setResultDescription(data.getResultDescription());
            result.setConclusion(data.getConclusion());
            result.setReferenceRange(data.getReferenceRange());
            result.setAbnormalFlags(data.getAbnormalFlags());
            result.setImageUrls(data.getImageUrls());
            result.setEquipment(data.getEquipment());
            result.setStatus("draft");
            examResultMapper.insert(result);
            log.info("创建检查结果: examResultId={}, examRequestId={}", result.getId(), examRequestId);
            return result;
        }
    }

    @Override
    @Transactional
    public ExamResult submitForReview(Long examResultId) {
        ExamResult result = examResultMapper.selectById(examResultId);
        if (result == null) throw new BusinessException("检查结果不存在");
        if (!"draft".equals(result.getStatus())) {
            throw new BusinessException("只有草稿状态的报告可以提交审核");
        }
        result.setStatus("submitted");
        result.setSubmittedAt(LocalDateTime.now());
        examResultMapper.updateById(result);
        log.info("提交审核: examResultId={}", examResultId);
        return result;
    }

    @Override
    @Transactional
    public ExamResult reviewResult(Long examResultId, Long reviewerId, String decision, String comment) {
        ExamResult result = examResultMapper.selectById(examResultId);
        if (result == null) throw new BusinessException("检查结果不存在");
        if (!"submitted".equals(result.getStatus())) {
            throw new BusinessException("只有已提交的报告可以审核");
        }

        result.setReviewerId(reviewerId);
        if ("approved".equals(decision)) {
            result.setStatus("reviewed");
            result.setReviewedAt(LocalDateTime.now());
            log.info("审核通过: examResultId={}, reviewerId={}", examResultId, reviewerId);
        } else if ("rejected".equals(decision)) {
            result.setStatus("draft"); // 驳回退回草稿
            log.info("审核驳回: examResultId={}, reviewerId={}, reason={}", examResultId, reviewerId, comment);
        } else {
            throw new BusinessException("无效的审核决定: " + decision);
        }
        examResultMapper.updateById(result);
        return result;
    }

    @Override
    @Transactional
    public ExamResult publishResult(Long examResultId) {
        ExamResult result = examResultMapper.selectById(examResultId);
        if (result == null) throw new BusinessException("检查结果不存在");
        if (!"reviewed".equals(result.getStatus())) {
            throw new BusinessException("只有已审核的报告可以发布");
        }

        result.setStatus("published");
        result.setPublishedAt(LocalDateTime.now());
        examResultMapper.updateById(result);

        // 更新申请单状态
        ExamRequest examRequest = examRequestMapper.selectById(result.getExamRequestId());
        if (examRequest != null) {
            examRequest.setStatus("completed");
            examRequestMapper.updateById(examRequest);
        }

        log.info("报告发布: examResultId={}, examRequestId={}", examResultId, result.getExamRequestId());

        // 发送Kafka消息通知医生端和患者端（WebSocket实时推送）
        if (examRequest != null) {
            messageProducer.sendReportPublishedNotification(
                    examRequest.getId(), examRequest.getPatientId(),
                    examRequest.getDoctorId(), examRequest.getExamName()
            );
        }

        return result;
    }

    @Override
    public IPage<ExamResult> getResults(String status, int page, int size) {
        LambdaQueryWrapper<ExamResult> wrapper = new LambdaQueryWrapper<ExamResult>()
                .orderByDesc(ExamResult::getCreatedAt);
        if (status != null) wrapper.eq(ExamResult::getStatus, status);
        return examResultMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public List<ExamResult> getPatientResults(Long patientId) {
        return examResultMapper.selectList(
                new LambdaQueryWrapper<ExamResult>()
                        .eq(ExamResult::getStatus, "published")
                        .inSql(ExamResult::getExamRequestId,
                                "SELECT id FROM exam_requests WHERE patient_id = " + patientId)
                        .orderByDesc(ExamResult::getPublishedAt));
    }
}
