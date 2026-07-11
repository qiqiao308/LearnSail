package com.smartcampus.common.dto;

import lombok.Data;

@Data
public class AnswerDTO {

    private Long questionId;

    private String userAnswer;
}
