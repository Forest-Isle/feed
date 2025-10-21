package com.senyu.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应结果
 *
 * @author senyu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 总数
     */
    private Long total;

    /**
     * 当前页
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 下一页游标（用于Feed流滚动加载）
     */
    private Long nextCursor;

    public PageResult(List<T> list, Long total, Integer page, Integer pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.hasNext = (long) page * pageSize < total;
    }

    public PageResult(List<T> list, Long nextCursor, Boolean hasNext) {
        this.list = list;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }
}
