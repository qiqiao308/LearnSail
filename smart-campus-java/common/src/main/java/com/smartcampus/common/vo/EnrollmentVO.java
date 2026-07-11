package com.smartcampus.common.vo;

import lombok.Data;

@Data
public class EnrollmentVO {

    private Long id;

    private Long courseId;

    private String courseTitle;

    private String courseCover;

    private String teacherName;

    private String status;

    private String enrollTime;

    private Double progress;

    private Integer completedSections;

    private Integer totalSections;
}
