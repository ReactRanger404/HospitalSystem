package com.health.system.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 药品列表视图对象（含库存量）
 * 替换原来 PharmacyServiceImpl 中的匿名 Object 类
 *
 * @author health-system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "药品列表（含库存）")
public class DrugVO {

    @Schema(description = "药品ID")
    private Long id;

    @Schema(description = "药品编码")
    private String code;

    @Schema(description = "药品名称")
    private String name;

    @Schema(description = "药品分类")
    private String drugCategory;

    @Schema(description = "规格")
    private String specification;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "生产厂家")
    private String manufacturer;

    @Schema(description = "零售价")
    private BigDecimal salePrice;

    @Schema(description = "最低库存预警")
    private Integer minStock;

    @Schema(description = "是否处方药")
    private Boolean isPrescription;

    @Schema(description = "当前总库存")
    private Integer totalStock;
}
