package com.health.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.health.system.entity.Department;
import com.health.system.mapper.DepartmentMapper;
import com.health.system.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 科室服务实现
 *
 * @author health-system
 */
@Slf4j
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Override
    @Cacheable(value = "departments", key = "'category:' + #category", unless = "#result.isEmpty()")
    public List<Department> getByCategory(String category) {
        return this.list(new LambdaQueryWrapper<Department>()
                .eq(Department::getCategory, category)
                .eq(Department::getIsActive, true));
    }

    @Override
    @Cacheable(value = "departments", key = "'active'")
    public List<Department> getActiveDepartments() {
        return this.list(new LambdaQueryWrapper<Department>()
                .eq(Department::getIsActive, true));
    }
}
