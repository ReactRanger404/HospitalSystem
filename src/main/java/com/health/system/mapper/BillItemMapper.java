package com.health.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.system.entity.BillItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账单明细 Mapper 接口
 *
 * @author health-system
 */
@Mapper
public interface BillItemMapper extends BaseMapper<BillItem> {
}
