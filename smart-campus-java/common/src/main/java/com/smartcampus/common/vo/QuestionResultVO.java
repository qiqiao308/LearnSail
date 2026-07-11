package com.smartcampus.common.vo;

import lombok.Data;

@Data
public class QuestionResultVO {

    private Long id;

    private String questionType;

    private String content;

    private String options;

    private String userAnswer;

    private String correctAnswer;

    private Integer isCorrect;

    private Integer score;

    private Integer questionScore;
}
