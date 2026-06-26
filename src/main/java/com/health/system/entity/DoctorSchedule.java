package com.health.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 医生排班实体
 * 管理各科室医生的出诊时间与号源数量
 *
 * @author health-system
 */
@Data
@TableName("doctor_schedules")
public class DoctorSchedule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 医生ID */
    private Long doctorId;

    /** 科室ID */
    private Long departmentId;

    /** 出诊日期 */
    private LocalDate scheduleDate;

    /** 时段: morning-上午, afternoon-下午, evening-晚间 */
    private String timeSlot;

    /** 开始时间 */
    private LocalTime startTime;

    /** 结束时间 */
    private LocalTime endTime;

    /** 最大号源数 */
    private Integer maxPatients;

    /** 已预约数 */
    private Integer bookedCount;

    /** 是否有效 */
    private Boolean isActive;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
