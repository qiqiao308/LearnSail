package com.smartcampus.common.vo;

import lombok.Data;

@Data
public class QuestionVO {

    private Long id;

    private String questionType;

    private String content;

    private String options;

    private Integer score;

    private String answer;

    private Integer sortOrder;
}
