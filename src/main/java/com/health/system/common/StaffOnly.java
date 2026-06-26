package com.health.system.common;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * 自定义权限注解 — 仅限内部人员（非患者）访问
 * 加在 Controller 方法上，患者角色调用将返回 403
 *
 * @author health-system
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole('admin', 'doctor', 'nurse', 'pharmacist', 'tech')")
public @interface StaffOnly {
}
