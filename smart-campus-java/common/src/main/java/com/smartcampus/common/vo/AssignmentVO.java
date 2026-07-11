package com.smartcampus.common.vo;

import lombok.Data;

@Data
public class AssignmentVO {

    private Long id;

    private Long courseId;

    private String courseTitle;

    private Long chapterId;

    private String title;

    private String description;

    private String deadline;

    private Integer maxScore;

    private String createTime;

    private Boolean isSubmitted;

    private Integer myScore;

    private String submissionStatus;
}
