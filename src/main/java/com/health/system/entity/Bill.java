package com.health.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账单实体
 * 记录患者的各类费用账单（挂号费、处方费、检查费、住院费）
 *
 * @author health-system
 */
@Data
@TableName("bills")
public class Bill {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 账单编号（自动生成） */
    private String billNo;

    /** 患者ID */
    private Long patientId;

    /** 账单类型: registration/prescription/examination/hospitalization */
    private String billType;

    /** 关联业务ID */
    private Long referenceId;

    /** 关联业务类型 */
    private String referenceType;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 优惠金额 */
    private BigDecimal discountAmount;

    /** 应付金额 */
    private BigDecimal payableAmount;

    /** 已付金额 */
    private BigDecimal paidAmount;

    /** 状态: pending/partially_paid/paid/refunded/cancelled */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 支付时间 */
    private LocalDateTime paidAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
