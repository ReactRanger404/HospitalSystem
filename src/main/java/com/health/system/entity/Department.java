package com.health.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 科室信息实体
 * 管理医院的各个科室，为排班、挂号等模块提供基础数据
 *
 * @author health-system
 */
@Data
@TableName("departments")
public class Department {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 科室名称 */
    private String name;

    /** 科室编码 */
    private String code;

    /** 科室类别: 临床/医技/药房/行政 */
    private String category;

    /** 科室简介 */
    private String description;

    /** 科室位置 */
    private String location;

    /** 联系电话 */
    private String phone;

    /** 是否启用 */
    private Boolean isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
