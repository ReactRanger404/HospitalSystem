package com.health.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.system.entity.PrescriptionItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 处方明细 Mapper 接口
 *
 * @author health-system
 */
@Mapper
public interface PrescriptionItemMapper extends BaseMapper<PrescriptionItem> {
}
