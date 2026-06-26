package com.health.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.system.entity.DrugTransaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品出入库记录 Mapper 接口
 *
 * @author health-system
 */
@Mapper
public interface DrugTransactionMapper extends BaseMapper<DrugTransaction> {
}
