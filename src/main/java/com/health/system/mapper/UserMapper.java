package com.health.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.system.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 *
 * @author health-system
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
