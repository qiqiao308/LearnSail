package com.smartcampus.common.vo;

import lombok.Data;

import java.util.List;

@Data
public class ExamResultVO {

    private Long examRecordId;

    private String examTitle;

    private Integer totalScore;

    private Integer passScore;

    private Integer myScore;

    private Boolean isPassed;

    private String status;

    private String startTime;

    private String submitTime;

    private List<QuestionRecordVO> questionRecords;
}
