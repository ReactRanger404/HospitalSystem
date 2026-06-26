package com.health.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.health.system.common.JwtUtil;
import com.health.system.common.exception.BusinessException;
import com.health.system.dto.request.LoginRequest;
import com.health.system.dto.response.LoginResponse;
import com.health.system.entity.User;
import com.health.system.enums.UserRole;
import com.health.system.mapper.UserMapper;
import com.health.system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现
 *
 * @author health-system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录: username={}", request.getUsername());

        // 查询用户
        User user = this.getByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!user.getIsActive()) {
            throw new BusinessException("账户已被禁用，请联系管理员");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("密码错误: username={}", request.getUsername());
            throw new BusinessException("用户名或密码错误");
        }

        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(),
                user.getRole().getValue());

        log.info("登录成功: userId={}, role={}", user.getId(), user.getRole());
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole().getValue())
                .departmentId(user.getDepartmentId())
                .build();
    }

    @Override
    public User register(User user) {
        // 检查用户名是否已存在
        if (this.getByUsername(user.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setIsActive(true);
        this.save(user);

        log.info("注册成功: userId={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());
        return user;
    }

    @Override
    @Cacheable(value = "user", key = "#username", unless = "#result == null")
    public User getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    @Override
    public List<User> getDoctorsByDepartment(Long departmentId) {
        return this.list(new LambdaQueryWrapper<User>()
                .eq(User::getDepartmentId, departmentId)
                .eq(User::getRole, UserRole.DOCTOR)
                .eq(User::getIsActive, true));
    }

    @Override
    public List<User> getPatients(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getRole, UserRole.PATIENT);
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getRealName, keyword)
                    .or().like(User::getPhone, keyword)
                    .or().like(User::getIdCard, keyword));
        }
        return this.list(wrapper);
    }
}
