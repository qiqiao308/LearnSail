package com.smartcampus.common.vo;

import lombok.Data;

@Data
public class SectionVO {

    private Long id;

    private Long chapterId;

    private Long courseId;

    private String title;

    private String contentType;

    private String contentUrl;

    private String contentText;

    private Integer duration;

    private Integer sortOrder;

    private Boolean isCompleted;
}
