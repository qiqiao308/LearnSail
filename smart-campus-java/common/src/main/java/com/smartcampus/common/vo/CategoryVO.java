package com.smartcampus.common.vo;

import lombok.Data;

import java.util.List;

@Data
public class CategoryVO {

    private Long id;

    private String name;

    private Long parentId;

    private Integer sortOrder;

    private List<CategoryVO> children;
}
