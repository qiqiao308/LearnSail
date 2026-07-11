package com.smartcampus.common.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {

    private Long total;

    private Long pages;

    private List<T> list;

    public static <T> PageVO<T> from(IPage<T> page) {
        return new PageVO<>(page.getTotal(), page.getPages(), page.getRecords());
    }
}
