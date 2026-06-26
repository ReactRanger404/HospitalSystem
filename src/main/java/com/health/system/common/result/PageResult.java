package com.health.system.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * 分页查询结果封装
 * 用于统一返回分页数据格式
 *
 * @param <T> 列表数据类型
 * @author health-system
 */
@Data
public class PageResult<T> {

    /** 数据列表 */
    private List<T> items;
    /** 总记录数 */
    private Long total;
    /** 当前页码 */
    private Integer page;
    /** 每页大小 */
    private Integer pageSize;
    /** 总页数 */
    private Long totalPages;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.items = page.getRecords();
        result.total = page.getTotal();
        result.page = (int) page.getCurrent();
        result.pageSize = (int) page.getSize();
        result.totalPages = page.getPages();
        return result;
    }

    public static <T> PageResult<T> of(List<T> items, Long total, Integer page, Integer pageSize) {
        PageResult<T> result = new PageResult<>();
        result.items = items;
        result.total = total;
        result.page = page;
        result.pageSize = pageSize;
        result.totalPages = (total + pageSize - 1) / pageSize;
        return result;
    }
}
