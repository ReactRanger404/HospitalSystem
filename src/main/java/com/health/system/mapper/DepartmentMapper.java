package com.health.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.system.entity.Department;
import org.apache.ibatis.annotations.Mapper;

/**
 * 科室 Mapper 接口
 *
 * @author health-system
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}
