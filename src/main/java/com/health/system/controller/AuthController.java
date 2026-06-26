package com.health.system.controller;

import com.health.system.common.result.Result;
import com.health.system.dto.request.LoginRequest;
import com.health.system.dto.response.LoginResponse;
import com.health.system.entity.User;
import com.health.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理登录、注册、用户信息查询
 *
 * @author health-system
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录注册与信息查询")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录请求: username={}", request.getUsername());
        LoginResponse response = userService.login(request);
        return Result.success("登录成功", response);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<User> register(@Valid @RequestBody User user) {
        User created = userService.register(user);
        created.setPassword(null); // 返回时脱敏
        return Result.success("注册成功", created);
    }

    @PostMapping("/register/patient")
    @Operation(summary = "患者自助注册")
    public Result<User> registerPatient(@Valid @RequestBody User user) {
        user.setRole(com.health.system.enums.UserRole.PATIENT);
        // 未设置密码时默认身份证后6位
        if (user.getPassword() == null && user.getIdCard() != null) {
            user.setPassword(user.getIdCard().substring(Math.max(0, user.getIdCard().length() - 6)));
        }
        User created = userService.register(user);
        created.setPassword(null);
        return Result.success("注册成功", created);
    }
}
