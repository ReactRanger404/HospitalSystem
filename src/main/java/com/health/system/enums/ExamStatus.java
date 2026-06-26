package com.health.system.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 检查检验申请状态枚举
 *
 * @author health-system
 */
@Getter
@AllArgsConstructor
public enum ExamStatus {

    PENDING("pending", "待接诊"),
    IN_PROGRESS("in_progress", "进行中"),
    COMPLETED("completed", "已完成"),
    CANCELLED("cancelled", "已取消");

    @EnumValue
    @JsonValue
    private final String value;
    private final String label;
}
