package com.health.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体
 * 记录每笔支付交易的详情（支持微信、支付宝、医保、现金）
 *
 * @author health-system
 */
@Data
@TableName("payments")
public class Payment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 账单ID */
    private Long billId;

    /** 支付流水号 */
    private String paymentNo;

    /** 支付金额 */
    private BigDecimal amount;

    /** 支付方式: wechat/alipay/insurance/cash/card */
    private String paymentMethod;

    /** 支付类型: full/partial/deposit/refund */
    private String paymentType;

    /** 第三方交易号 */
    private String transactionId;

    /** 状态: pending/success/failed/refunded */
    private String status;

    /** 操作员（收款员）ID */
    private Long operatorId;

    /** 备注 */
    private String note;

    /** 支付时间 */
    private LocalDateTime paidAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
