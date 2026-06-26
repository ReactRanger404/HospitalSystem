package com.health.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.health.system.entity.Department;

import java.util.List;

/**
 * 科室服务接口
 *
 * @author health-system
 */
public interface DepartmentService extends IService<Department> {

    /**
     * 按类别查询科室列表
     */
    List<Department> getByCategory(String category);

    /**
     * 获取启用的科室列表
     */
    List<Department> getActiveDepartments();
}
