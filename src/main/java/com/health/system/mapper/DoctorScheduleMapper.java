package com.health.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.system.entity.DoctorSchedule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医生排班 Mapper 接口
 *
 * @author health-system
 */
@Mapper
public interface DoctorScheduleMapper extends BaseMapper<DoctorSchedule> {
}
