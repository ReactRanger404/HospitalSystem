package com.health.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.system.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约挂号 Mapper 接口
 *
 * @author health-system
 */
@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
}
