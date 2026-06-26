package com.health.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.system.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付记录 Mapper 接口
 *
 * @author health-system
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
