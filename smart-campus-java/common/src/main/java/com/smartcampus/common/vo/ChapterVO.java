package com.smartcampus.common.vo;

import lombok.Data;

import java.util.List;

@Data
public class ChapterVO {

    private Long id;

    private Long courseId;

    private String title;

    private Integer sortOrder;

    private List<SectionVO> sections;
}
