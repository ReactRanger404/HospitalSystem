package com.health.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 检查检验申请实体
 * 医生开具的检查（CT/核磁/B超等）和检验（血常规/尿检等）申请单
 *
 * @author health-system
 */
@Data
@TableName("exam_requests")
public class ExamRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者ID */
    private Long patientId;

    /** 开单医生ID */
    private Long doctorId;

    /** 关联病历ID */
    private Long medicalRecordId;

    /** 申请类型: examination-检查, lab_test-检验 */
    private String requestType;

    /** 检查检验类别: CT/MRI/X-ray/Ultrasound/Blood/Urine/Biochemistry等 */
    private String examCategory;

    /** 检查检验项目名称（如: 头颅CT平扫、血常规五分类） */
    private String examName;

    /** 状态: pending/in_progress/completed/cancelled */
    private String status;

    /** 临床诊断 */
    private String clinicalDiagnosis;

    /** 临床备注 */
    private String clinicalNote;

    /** 紧急程度: emergency/urgent/routine */
    private String urgency;

    /** 是否门诊 */
    private Boolean isOutpatient;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
