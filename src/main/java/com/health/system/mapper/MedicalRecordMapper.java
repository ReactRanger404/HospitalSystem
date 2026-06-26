package com.health.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.system.entity.MedicalRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 电子病历 Mapper 接口
 *
 * @author health-system
 */
@Mapper
public interface MedicalRecordMapper extends BaseMapper<MedicalRecord> {
}
