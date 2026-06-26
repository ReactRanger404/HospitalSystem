package com.health.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.health.system.enums.PrescriptionStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 处方实体
 * 存储医生开具的处方主信息，支持西药和中药
 *
 * @author health-system
 */
@Data
@TableName("prescriptions")
public class Prescription {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者ID */
    private Long patientId;

    /** 医生ID */
    private Long doctorId;

    /** 关联病历ID */
    private Long medicalRecordId;

    /** 处方类型: western-西药, chinese-中药 */
    private String prescriptionType;

    /** 状态: pending/paid/dispensing/dispensed/cancelled */
    private PrescriptionStatus status;

    /** 煎药方法（中药） */
    private String decoctionMethod;

    /** 煎药说明（中药） */
    private String decoctionNote;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
