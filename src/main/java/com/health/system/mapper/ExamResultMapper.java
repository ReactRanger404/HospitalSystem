package com.health.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.system.entity.ExamResult;
import org.apache.ibatis.annotations.Mapper;

/**
 * 检查检验结果 Mapper 接口
 *
 * @author health-system
 */
@Mapper
public interface ExamResultMapper extends BaseMapper<ExamResult> {
}
