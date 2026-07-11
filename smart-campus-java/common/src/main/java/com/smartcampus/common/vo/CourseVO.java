package com.smartcampus.common.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseVO {

    private Long id;

    private String title;

    private String description;

    private String coverImage;

    private Long categoryId;

    private String categoryName;

    private Long teacherId;

    private String teacherName;

    private BigDecimal price;

    private String status;

    private Integer studentCount;

    private Integer chapterCount;

    private String createTime;
}
