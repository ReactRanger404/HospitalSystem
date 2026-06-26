package com.health.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.health.system.common.result.PageResult;
import com.health.system.common.result.Result;
import com.health.system.entity.*;
import com.health.system.service.DoctorStationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 门诊医生站控制器
 * 电子病历、处方开具、医技申请、历史调阅
 *
 * @author health-system
 */
@Slf4j
@RestController
@RequestMapping("/doctor")
@RequiredArgsConstructor
@Tag(name = "门诊医生站", description = "电子病历管理、处方开具、检查检验申请、患者历史调阅")
public class DoctorStationController {

    private final DoctorStationService doctorStationService;

    // ====== 待诊患者 ======
    @GetMapping("/patients/waiting")
    @Operation(summary = "获取今日待诊患者列表")
    public Result<List<Appointment>> getWaitingPatients(@RequestParam Long doctorId) {
        return Result.success(doctorStationService.getTodayWaitingPatients(doctorId));
    }

    @PutMapping("/appointments/{id}/start")
    @Operation(summary = "开始就诊")
    public Result<Void> startConsultation(@PathVariable Long id) {
        doctorStationService.startConsultation(id);
        return Result.success();
    }

    @PutMapping("/appointments/{id}/complete")
    @Operation(summary = "完成就诊")
    public Result<Void> completeConsultation(@PathVariable Long id) {
        doctorStationService.completeConsultation(id);
        return Result.success();
    }

    // ====== 电子病历（仅医生可操作） ======
    @PostMapping("/medical-records")
    @PreAuthorize("hasRole('doctor')")
    @Operation(summary = "创建电子病历")
    public Result<MedicalRecord> createMedicalRecord(@RequestBody MedicalRecord record,
                                                     Authentication auth) {
        if (auth != null) record.setDoctorId((Long) auth.getPrincipal());
        return Result.success(doctorStationService.createMedicalRecord(record));
    }

    @PutMapping("/medical-records/{id}")
    @Operation(summary = "更新电子病历")
    public Result<MedicalRecord> updateMedicalRecord(@PathVariable Long id,
                                                     @RequestBody MedicalRecord record) {
        return Result.success(doctorStationService.updateMedicalRecord(id, record));
    }

    @GetMapping("/medical-records/{id}")
    @Operation(summary = "获取病历详情")
    public Result<MedicalRecord> getMedicalRecord(@PathVariable Long id) {
        return Result.success(doctorStationService.getMedicalRecordById(id));
    }

    @GetMapping("/medical-records")
    @Operation(summary = "查询患者的就诊记录")
    public Result<PageResult<MedicalRecord>> getPatientRecords(
            @RequestParam Long patientId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<MedicalRecord> result = doctorStationService.getPatientRecords(patientId, page, size);
        return Result.success(PageResult.of(result));
    }

    // ====== 处方（仅医生可开具） ======
    @PostMapping("/prescriptions")
    @PreAuthorize("hasRole('doctor')")
    @Operation(summary = "开具处方")
    public Result<Prescription> createPrescription(@RequestBody Map<String, Object> params) {
        // 解析嵌套的 JSON 参数
        // 在实际项目中应使用 DTO
        Prescription prescription = new Prescription();
        prescription.setPatientId(Long.valueOf(params.get("patientId").toString()));
        prescription.setPrescriptionType((String) params.get("prescriptionType"));
        if (params.get("medicalRecordId") != null) {
            prescription.setMedicalRecordId(Long.valueOf(params.get("medicalRecordId").toString()));
        }
        if (params.get("remark") != null) {
            prescription.setRemark((String) params.get("remark"));
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) params.get("items");
        List<PrescriptionItem> items = itemsData.stream().map(m -> {
            PrescriptionItem item = new PrescriptionItem();
            item.setDrugId(Long.valueOf(m.get("drugId").toString()));
            item.setDrugName((String) m.get("drugName"));
            item.setDosage((String) m.get("dosage"));
            item.setFrequency((String) m.get("frequency"));
            item.setDays(Integer.valueOf(m.get("days").toString()));
            item.setQuantity(Integer.valueOf(m.get("quantity").toString()));
            item.setUnit((String) m.get("unit"));
            item.setUsageMethod((String) m.get("usageMethod"));
            item.setUsageInstruction((String) m.get("usageInstruction"));
            if (m.get("unitPrice") != null) {
                item.setUnitPrice(new java.math.BigDecimal(m.get("unitPrice").toString()));
                item.setAmount(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())));
            }
            return item;
        }).toList();

        return Result.success(doctorStationService.createPrescription(prescription, items));
    }

    @GetMapping("/prescriptions")
    @Operation(summary = "查询处方列表")
    public Result<PageResult<Prescription>> getPrescriptions(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<Prescription> result = doctorStationService.getPrescriptions(patientId, doctorId, status, page, size);
        return Result.success(PageResult.of(result));
    }

    // ====== 医技申请（仅医生可开单） ======
    @PostMapping("/exam-requests")
    @PreAuthorize("hasRole('doctor')")
    @Operation(summary = "创建检查检验申请")
    public Result<ExamRequest> createExamRequest(@RequestBody ExamRequest examRequest,
                                                 Authentication auth) {
        if (auth != null) examRequest.setDoctorId((Long) auth.getPrincipal());
        return Result.success(doctorStationService.createExamRequest(examRequest));
    }

    @GetMapping("/exam-requests")
    @Operation(summary = "查询检查检验申请")
    public Result<PageResult<ExamRequest>> getExamRequests(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<ExamRequest> result = doctorStationService.getExamRequests(patientId, status, page, size);
        return Result.success(PageResult.of(result));
    }

    // ====== 历史调阅 ======
    @GetMapping("/patients/{patientId}/history")
    @Operation(summary = "调阅患者完整诊疗历史")
    public Result<Map<String, Object>> getPatientHistory(@PathVariable Long patientId) {
        return Result.success(doctorStationService.getPatientHistory(patientId));
    }
}
