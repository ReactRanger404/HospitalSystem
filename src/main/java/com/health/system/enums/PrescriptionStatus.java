package com.health.system.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 处方状态枚举
 *
 * @author health-system
 */
@Getter
@AllArgsConstructor
public enum PrescriptionStatus {

    PENDING("pending", "待缴费"),
    PAID("paid", "已缴费"),
    DISPENSING("dispensing", "配药中"),
    DISPENSED("dispensed", "已发药"),
    PARTIALLY_DISPENSED("partially_dispensed", "部分发药"),
    CANCELLED("cancelled", "已取消");

    @EnumValue
    @JsonValue
    private final String value;
    private final String label;
}
