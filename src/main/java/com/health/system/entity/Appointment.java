package com.health.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.health.system.enums.AppointmentStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约挂号实体
 * 管理患者的预约挂号、分诊到诊、退号取消
 *
 * @author health-system
 */
@Data
@TableName("appointments")
public class Appointment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者ID */
    private Long patientId;

    /** 医生ID */
    private Long doctorId;

    /** 科室ID */
    private Long departmentId;

    /** 排班ID */
    private Long scheduleId;

    /** 预约日期 */
    private LocalDate appointmentDate;

    /** 预约时段 */
    private String timeSlot;

    /** 排队序号 */
    private Integer queueNumber;

    /** 状态: pending/checked_in/in_consultation/completed/cancelled/refunded */
    private AppointmentStatus status;

    /** 挂号来源: wechat/app/kiosk/onsite */
    private String source;

    /** 症状描述 */
    private String symptoms;

    /** 是否初诊 */
    private Boolean isFirstVisit;

    /** 取消原因 */
    private String cancelReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
