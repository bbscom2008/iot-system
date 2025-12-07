package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> list;
    private Long total;
    private Integer pageNum;     // 当前页码
    private Integer pageSize;    // 每页大小

    public static <T> PageResult<T> of(List<T> list, Long total) {
        return new PageResult<>(list, total, null, null);
    }

    public static <T> PageResult<T> of(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        return new PageResult<>(list, total, pageNum, pageSize);
    }
}

