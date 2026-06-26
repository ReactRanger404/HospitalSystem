package com.health.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 药品库存实体
 * 按批次管理药品库存，支持有效期预警
 *
 * @author health-system
 */
@Data
@TableName("drug_inventories")
public class DrugInventory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 药品ID */
    private Long drugId;

    /** 批号 */
    private String batchNumber;

    /** 当前库存数量 */
    private Integer quantity;

    /** 采购单价 */
    private BigDecimal purchasePrice;

    /** 零售单价 */
    private BigDecimal salePrice;

    /** 生产日期 */
    private LocalDate productionDate;

    /** 有效期至 */
    private LocalDate expiryDate;

    /** 供应商 */
    private String supplier;

    /** 货位 */
    private String location;

    /** 是否有效 */
    private Boolean isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
