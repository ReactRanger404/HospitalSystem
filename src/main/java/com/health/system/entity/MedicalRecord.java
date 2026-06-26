package com.health.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 电子病历实体（EMR）
 * 记录患者的主诉、现病史、既往史、诊断结果等完整诊疗信息
 *
 * @author health-system
 */
@Data
@TableName("medical_records")
public class MedicalRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者ID */
    private Long patientId;

    /** 医生ID */
    private Long doctorId;

    /** 关联预约ID */
    private Long appointmentId;

    /** 就诊日期 */
    private LocalDate visitDate;

    /** 就诊类型: outpatient/inpatient/emergency */
    private String visitType;

    /** 主诉 - 患者主要症状及持续时间 */
    private String chiefComplaint;

    /** 现病史 - 疾病发生发展过程 */
    private String presentIllness;

    /** 既往史 - 过去健康状况 */
    private String pastHistory;

    /** 个人史 */
    private String personalHistory;

    /** 家族史 */
    private String familyHistory;

    /** 过敏史 */
    private String allergyHistory;

    /** 体格检查结果 */
    private String physicalExamination;

    /** 生命体征（JSON: 体温/脉搏/呼吸/血压） */
    private String vitalSigns;

    /** 诊断结果 */
    private String diagnosis;

    /** 诊断编码（ICD-10） */
    private String diagnosisCode;

    /** 治疗方案 */
    private String treatmentPlan;

    /** 医生建议 */
    private String doctorAdvice;

    /** 随访建议 */
    private String followUp;

    /** 是否已归档 */
    private Boolean isFinalized;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
