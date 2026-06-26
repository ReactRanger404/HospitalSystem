package com.health.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.system.entity.Bill;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账单 Mapper 接口
 *
 * @author health-system
 */
@Mapper
public interface BillMapper extends BaseMapper<Bill> {
}
