package com.health.system.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账单状态枚举
 *
 * @author health-system
 */
@Getter
@AllArgsConstructor
public enum BillStatus {

    PENDING("pending", "待缴费"),
    PARTIALLY_PAID("partially_paid", "部分支付"),
    PAID("paid", "已支付"),
    REFUNDED("refunded", "已退款"),
    CANCELLED("cancelled", "已取消");

    @EnumValue
    @JsonValue
    private final String value;
    private final String label;
}
