package com.health.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品基本信息实体
 * 管理药品目录，包括西药、中药、中成药
 *
 * @author health-system
 */
@Data
@TableName("drugs")
public class Drug {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 药品编码 */
    private String code;

    /** 药品名称 */
    private String name;

    /** 通用名 */
    private String genericName;

    /** 英文名 */
    private String englishName;

    /** 药品分类: western/chinese/chinese_patent */
    private String drugCategory;

    /** 剂型: tablet/capsule/injection/liquid/ointment/granule */
    private String dosageForm;

    /** 药品规格（如: 0.5g*24片） */
    private String specification;

    /** 单位（盒/瓶/袋/支） */
    private String unit;

    /** 生产厂家 */
    private String manufacturer;

    /** 批准文号 */
    private String approvalNumber;

    /** 采购单价 */
    private BigDecimal purchasePrice;

    /** 零售单价 */
    private BigDecimal salePrice;

    /** 最低库存预警 */
    private Integer minStock;

    /** 最高库存上限 */
    private Integer maxStock;

    /** 是否处方药 */
    private Boolean isPrescription;

    /** 是否启用 */
    private Boolean isActive;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
