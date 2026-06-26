package com.health.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 处方明细实体
 * 记录处方中的每种药品及用法、用量
 *
 * @author health-system
 */
@Data
@TableName("prescription_items")
public class PrescriptionItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 处方ID */
    private Long prescriptionId;

    /** 药品ID */
    private Long drugId;

    /** 药品名称 */
    private String drugName;

    /** 药品规格 */
    private String specification;

    /** 单次用量 */
    private String dosage;

    /** 使用频次（如 tid/bid） */
    private String frequency;

    /** 频次说明 */
    private String frequencyDetail;

    /** 用药天数 */
    private Integer days;

    /** 数量 */
    private Integer quantity;

    /** 单位 */
    private String unit;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 金额 */
    private BigDecimal amount;

    /** 给药途径: oral/injection/external/inhalation */
    private String usageMethod;

    /** 用药指导 */
    private String usageInstruction;

    /** 用药模式: daily/odd/even */
    private String dayPattern;

    /** 备注 */
    private String note;

    /** 前置审核状态: pending/passed/rejected */
    private String auditStatus;

    /** 审核意见 */
    private String auditNote;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
