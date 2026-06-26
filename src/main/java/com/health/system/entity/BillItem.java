package com.health.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账单明细实体
 * 账单的细项：挂号费、处方药费、检查费等
 *
 * @author health-system
 */
@Data
@TableName("bill_items")
public class BillItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 账单ID */
    private Long billId;

    /** 项目类型: registration/prescription/examination */
    private String itemType;

    /** 项目ID（处方ID/检查ID等） */
    private Long itemId;

    /** 项目名称 */
    private String itemName;

    /** 数量 */
    private Integer quantity;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 金额 */
    private BigDecimal amount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
