package com.health.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.system.entity.ExamRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 检查检验申请 Mapper 接口
 *
 * @author health-system
 */
@Mapper
public interface ExamRequestMapper extends BaseMapper<ExamRequest> {
}
