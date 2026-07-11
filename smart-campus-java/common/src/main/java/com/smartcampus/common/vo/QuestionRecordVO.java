package com.smartcampus.common.vo;

import lombok.Data;

@Data
public class QuestionRecordVO {

    private Long questionId;

    private String questionType;

    private String content;

    private String options;

    private String userAnswer;

    private String correctAnswer;

    private Boolean isCorrect;

    private Integer score;

    private Integer totalQuestionScore;
}
