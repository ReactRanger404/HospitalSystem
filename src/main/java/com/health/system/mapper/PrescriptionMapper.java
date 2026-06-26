package com.health.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.system.entity.Prescription;
import org.apache.ibatis.annotations.Mapper;

/**
 * 处方 Mapper 接口
 *
 * @author health-system
 */
@Mapper
public interface PrescriptionMapper extends BaseMapper<Prescription> {
}
