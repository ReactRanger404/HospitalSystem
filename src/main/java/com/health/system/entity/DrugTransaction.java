package com.health.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品出入库记录实体
 * 药品流转审计追踪：采购入库、发药出库、调拨、盘点调整
 *
 * @author health-system
 */
@Data
@TableName("drug_transactions")
public class DrugTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 药品ID */
    private Long drugId;

    /** 批次库存ID */
    private Long inventoryId;

    /** 交易类型: purchase/dispense/return/transfer_in/transfer_out/adjustment */
    private String transactionType;

    /** 数量（正数入库/负数出库） */
    private Integer quantity;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 总金额 */
    private BigDecimal totalPrice;

    /** 操作人ID */
    private Long operatorId;

    /** 关联单据类型 */
    private String referenceType;

    /** 关联单据ID */
    private Long referenceId;

    /** 备注 */
    private String note;

    /** 交易时间 */
    private LocalDateTime transactionDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
