package com.health.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.system.entity.InpatientAccount;
import org.apache.ibatis.annotations.Mapper;

/**
 * 住院账户 Mapper 接口
 *
 * @author health-system
 */
@Mapper
public interface InpatientAccountMapper extends BaseMapper<InpatientAccount> {
}
