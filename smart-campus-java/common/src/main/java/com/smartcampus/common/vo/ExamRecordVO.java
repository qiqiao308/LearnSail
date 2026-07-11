package com.smartcampus.common.vo;

import lombok.Data;

@Data
public class ExamRecordVO {

    private Long id;

    private Long examId;

    private Long userId;

    private String studentName;

    private Integer score;

    private String status;

    private String startTime;

    private String submitTime;
}
