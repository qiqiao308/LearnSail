package com.smartcampus.common.vo;

import lombok.Data;

@Data
public class ExamVO {

    private Long id;

    private Long courseId;

    private String courseTitle;

    private String title;

    private String description;

    private Integer durationMinutes;

    private Integer totalScore;

    private Integer passScore;

    private String startTime;

    private String endTime;

    private String status;

    private Integer questionCount;

    private String myStatus;

    private Integer myScore;
}
