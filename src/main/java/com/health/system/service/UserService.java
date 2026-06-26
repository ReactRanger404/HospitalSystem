package com.health.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.health.system.dto.request.LoginRequest;
import com.health.system.dto.response.LoginResponse;
import com.health.system.entity.User;

/**
 * 用户服务接口
 *
 * @author health-system
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     * @param request 登录请求
     * @return 登录响应（含JWT令牌）
     */
    LoginResponse login(LoginRequest request);

    /**
     * 注册新用户
     * @param user 用户信息
     * @return 注册后的用户
     */
    User register(User user);

    /**
     * 根据用户名查询
     */
    User getByUsername(String username);

    /**
     * 根据科室ID查询医生列表
     */
    java.util.List<User> getDoctorsByDepartment(Long departmentId);

    /**
     * 查询患者列表
     */
    java.util.List<User> getPatients(String keyword);
}
