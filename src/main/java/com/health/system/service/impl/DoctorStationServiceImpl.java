package com.health.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.system.common.exception.BusinessException;
import com.health.system.entity.*;
import com.health.system.enums.AppointmentStatus;
import com.health.system.mapper.*;
import com.health.system.service.DoctorStationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 门诊医生站服务实现
 *
 * @author health-system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorStationServiceImpl implements DoctorStationService {

    private final MedicalRecordMapper medicalRecordMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final ExamRequestMapper examRequestMapper;
    private final AppointmentMapper appointmentMapper;

    // ====== 电子病历 ======

    @Override
    @Transactional
    public MedicalRecord createMedicalRecord(MedicalRecord record) {
        record.setVisitDate(LocalDate.now());
        record.setIsFinalized(false);
        medicalRecordMapper.insert(record);
        log.info("创建病历成功: id={}, patientId={}, doctorId={}",
                record.getId(), record.getPatientId(), record.getDoctorId());
        return record;
    }

    @Override
    @Transactional
    public MedicalRecord updateMedicalRecord(Long id, MedicalRecord record) {
        MedicalRecord existing = medicalRecordMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("病历不存在");
        }
        if (existing.getIsFinalized()) {
            throw new BusinessException("病历已归档，不可修改");
        }

        // 更新非空字段
        if (record.getChiefComplaint() != null) existing.setChiefComplaint(record.getChiefComplaint());
        if (record.getPresentIllness() != null) existing.setPresentIllness(record.getPresentIllness());
        if (record.getPastHistory() != null) existing.setPastHistory(record.getPastHistory());
        if (record.getDiagnosis() != null) existing.setDiagnosis(record.getDiagnosis());
        if (record.getDiagnosisCode() != null) existing.setDiagnosisCode(record.getDiagnosisCode());
        if (record.getTreatmentPlan() != null) existing.setTreatmentPlan(record.getTreatmentPlan());
        if (record.getDoctorAdvice() != null) existing.setDoctorAdvice(record.getDoctorAdvice());
        if (record.getPhysicalExamination() != null) existing.setPhysicalExamination(record.getPhysicalExamination());
        if (record.getVitalSigns() != null) existing.setVitalSigns(record.getVitalSigns());
        if (record.getIsFinalized() != null) existing.setIsFinalized(record.getIsFinalized());

        medicalRecordMapper.updateById(existing);
        log.info("更新病历成功: id={}", id);
        return existing;
    }

    @Override
    public MedicalRecord getMedicalRecordById(Long id) {
        MedicalRecord record = medicalRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("病历不存在");
        }
        return record;
    }

    @Override
    public IPage<MedicalRecord> getPatientRecords(Long patientId, int page, int size) {
        return medicalRecordMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<MedicalRecord>()
                        .eq(MedicalRecord::getPatientId, patientId)
                        .orderByDesc(MedicalRecord::getVisitDate));
    }

    // ====== 处方 ======

    @Override
    @Transactional
    public Prescription createPrescription(Prescription prescription, List<PrescriptionItem> items) {
        // 计算总金额
        BigDecimal total = items.stream()
                .map(item -> item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        prescription.setTotalAmount(total);
        prescriptionMapper.insert(prescription);

        // 插入处方明细
        for (PrescriptionItem item : items) {
            item.setPrescriptionId(prescription.getId());
            if (item.getAmount() == null) {
                item.setAmount(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
            prescriptionItemMapper.insert(item);
        }

        log.info("开具处方成功: id={}, patientId={}, type={}, items={}",
                prescription.getId(), prescription.getPatientId(),
                prescription.getPrescriptionType(), items.size());
        return prescription;
    }

    @Override
    public IPage<Prescription> getPrescriptions(Long patientId, Long doctorId, String status, int page, int size) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<Prescription>()
                .orderByDesc(Prescription::getCreatedAt);
        if (patientId != null) wrapper.eq(Prescription::getPatientId, patientId);
        if (doctorId != null) wrapper.eq(Prescription::getDoctorId, doctorId);
        if (status != null) wrapper.eq(Prescription::getStatus, status);
        return prescriptionMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ====== 医技申请 ======

    @Override
    @Transactional
    public ExamRequest createExamRequest(ExamRequest examRequest) {
        examRequest.setStatus("pending");
        examRequestMapper.insert(examRequest);
        log.info("创建检查申请: id={}, type={}, category={}, patientId={}",
                examRequest.getId(), examRequest.getRequestType(),
                examRequest.getExamCategory(), examRequest.getPatientId());
        return examRequest;
    }

    @Override
    public IPage<ExamRequest> getExamRequests(Long patientId, String status, int page, int size) {
        LambdaQueryWrapper<ExamRequest> wrapper = new LambdaQueryWrapper<ExamRequest>()
                .orderByDesc(ExamRequest::getCreatedAt);
        if (patientId != null) wrapper.eq(ExamRequest::getPatientId, patientId);
        if (status != null) wrapper.eq(ExamRequest::getStatus, status);
        return examRequestMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ====== 历史调阅 ======

    @Override
    public Map<String, Object> getPatientHistory(Long patientId) {
        Map<String, Object> result = new HashMap<>();

        List<MedicalRecord> records = medicalRecordMapper.selectList(
                new LambdaQueryWrapper<MedicalRecord>()
                        .eq(MedicalRecord::getPatientId, patientId)
                        .orderByDesc(MedicalRecord::getVisitDate)
                        .last("LIMIT 10"));
        result.put("medicalRecords", records);

        List<Prescription> prescriptions = prescriptionMapper.selectList(
                new LambdaQueryWrapper<Prescription>()
                        .eq(Prescription::getPatientId, patientId)
                        .orderByDesc(Prescription::getCreatedAt)
                        .last("LIMIT 10"));
        result.put("prescriptions", prescriptions);

        List<ExamRequest> examRequests = examRequestMapper.selectList(
                new LambdaQueryWrapper<ExamRequest>()
                        .eq(ExamRequest::getPatientId, patientId)
                        .orderByDesc(ExamRequest::getCreatedAt)
                        .last("LIMIT 10"));
        result.put("examRequests", examRequests);

        return result;
    }

    // ====== 待诊患者 ======

    @Override
    public List<Appointment> getTodayWaitingPatients(Long doctorId) {
        return appointmentMapper.selectList(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getDoctorId, doctorId)
                        .eq(Appointment::getAppointmentDate, LocalDate.now())
                        .in(Appointment::getStatus, AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_CONSULTATION)
                        .orderByAsc(Appointment::getQueueNumber));
    }

    @Override
    @Transactional
    public void startConsultation(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) throw new BusinessException("预约记录不存在");
        appointment.setStatus(AppointmentStatus.IN_CONSULTATION);
        appointmentMapper.updateById(appointment);
        log.info("开始就诊: appointmentId={}", appointmentId);
    }

    @Override
    @Transactional
    public void completeConsultation(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) throw new BusinessException("预约记录不存在");
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentMapper.updateById(appointment);
        log.info("完成就诊: appointmentId={}", appointmentId);
    }
}
