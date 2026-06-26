package com.health.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.system.entity.DrugInventory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品库存 Mapper 接口
 *
 * @author health-system
 */
@Mapper
public interface DrugInventoryMapper extends BaseMapper<DrugInventory> {
}
