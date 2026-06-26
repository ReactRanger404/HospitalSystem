package com.health.system.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 *
 * @author health-system
 */
@Getter
@AllArgsConstructor
public enum UserRole {

    DOCTOR("doctor", "医生"),
    NURSE("nurse", "护士"),
    PATIENT("patient", "患者"),
    ADMIN("admin", "管理员"),
    TECH("tech", "医技人员"),
    PHARMACIST("pharmacist", "药剂师");

    @EnumValue
    @JsonValue
    private final String value;
    private final String label;
}
