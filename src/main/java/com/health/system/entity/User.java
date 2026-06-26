package com.health.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.health.system.enums.UserRole;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体（统一管理医生、护士、患者、管理员等所有角色）
 * 通过 role 字段区分不同身份
 *
 * @author health-system
 */
@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名 */
    private String username;

    /** 密码哈希（BCrypt加密） */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 角色: doctor/nurse/patient/admin/tech/pharmacist */
    private UserRole role;

    /** 性别 */
    private String gender;

    /** 手机号 */
    private String phone;

    /** 电子邮箱 */
    private String email;

    /** 身份证号 */
    private String idCard;

    /** 出生日期 */
    private LocalDate birthDate;

    /** 家庭住址 */
    private String address;

    /** 所属科室ID */
    private Long departmentId;

    /** 职称（主任医师/副主任医师/主治医师等） */
    private String title;

    /** 执业证号 */
    private String licenseNumber;

    /** 专业特长 */
    private String specialization;

    /** 血型 */
    private String bloodType;

    /** 过敏史 */
    private String allergies;

    /** 既往病史摘要 */
    private String medicalHistory;

    /** 紧急联系人 */
    private String emergencyContact;

    /** 紧急联系电话 */
    private String emergencyPhone;

    /** 是否启用 */
    private Boolean isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
