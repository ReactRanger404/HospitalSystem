package com.health.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.health.system.entity.*;

/**
 * 门诊医生站服务接口
 * 电子病历、处方开具、医技申请、历史调阅
 *
 * @author health-system
 */
public interface DoctorStationService {

    // ====== 电子病历 ======
    MedicalRecord createMedicalRecord(MedicalRecord record);
    MedicalRecord updateMedicalRecord(Long id, MedicalRecord record);
    MedicalRecord getMedicalRecordById(Long id);
    IPage<MedicalRecord> getPatientRecords(Long patientId, int page, int size);

    // ====== 处方 ======
    Prescription createPrescription(Prescription prescription, java.util.List<PrescriptionItem> items);
    IPage<Prescription> getPrescriptions(Long patientId, Long doctorId, String status, int page, int size);

    // ====== 医技申请 ======
    ExamRequest createExamRequest(ExamRequest examRequest);
    IPage<ExamRequest> getExamRequests(Long patientId, String status, int page, int size);

    // ====== 历史调阅 ======
    Object getPatientHistory(Long patientId);

    // ====== 待诊患者 ======
    java.util.List<Appointment> getTodayWaitingPatients(Long doctorId);
    void startConsultation(Long appointmentId);
    void completeConsultation(Long appointmentId);
}
