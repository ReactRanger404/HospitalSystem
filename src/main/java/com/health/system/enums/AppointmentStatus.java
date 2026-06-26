package com.health.system.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 预约挂号状态枚举
 *
 * @author health-system
 */
@Getter
@AllArgsConstructor
public enum AppointmentStatus {

    PENDING("pending", "待就诊"),
    CHECKED_IN("checked_in", "已签到"),
    IN_CONSULTATION("in_consultation", "就诊中"),
    COMPLETED("completed", "已完成"),
    CANCELLED("cancelled", "已取消"),
    REFUNDED("refunded", "已退号");

    @EnumValue  // MyBatis-Plus 存储用
    @JsonValue // JSON 序列化用
    private final String value;
    private final String label;
}
