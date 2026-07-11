package com.smartcampus.common.vo;

import lombok.Data;

@Data
public class AssignmentSubmissionVO {

    private Long id;

    private Long assignmentId;

    private String assignmentTitle;

    private Long userId;

    private String studentName;

    private String content;

    private String fileUrl;

    private Integer score;

    private String comment;

    private String submitTime;

    private String gradeTime;
}
