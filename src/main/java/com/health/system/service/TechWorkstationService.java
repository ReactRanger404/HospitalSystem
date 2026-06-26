package com.health.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.health.system.entity.ExamRequest;
import com.health.system.entity.ExamResult;

import java.util.List;

/**
 * 医技工作站服务接口
 * 登记接诊、报告录入与审核、报告发布
 *
 * @author health-system
 */
public interface TechWorkstationService {

    /** 获取待接诊申请列表 */
    IPage<ExamRequest> getPendingExams(String examCategory, int page, int size);

    /** 登记接诊 */
    ExamRequest startExam(Long examRequestId, Long technicianId);

    /** 保存检查结果 */
    ExamResult saveResult(Long examRequestId, Long technicianId, ExamResult result);

    /** 提交审核 */
    ExamResult submitForReview(Long examResultId);

    /** 审核报告 */
    ExamResult reviewResult(Long examResultId, Long reviewerId, String decision, String comment);

    /** 发布报告 */
    ExamResult publishResult(Long examResultId);

    /** 查询检查结果 */
    IPage<ExamResult> getResults(String status, int page, int size);

    /** 获取患者已发布的检查结果 */
    List<ExamResult> getPatientResults(Long patientId);
}
