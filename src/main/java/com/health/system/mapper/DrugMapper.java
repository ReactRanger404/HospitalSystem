package com.health.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.system.entity.Drug;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品信息 Mapper 接口
 *
 * @author health-system
 */
@Mapper
public interface DrugMapper extends BaseMapper<Drug> {
}
