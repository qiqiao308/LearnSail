package com.smartcampus.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionSaveDTO {

    private Long id;

    @NotNull(message = "考试ID不能为空")
    private Long examId;

    @NotBlank(message = "题目类型不能为空")
    private String questionType;

    @NotBlank(message = "题目内容不能为空")
    private String content;

    private String options;

    @NotBlank(message = "答案不能为空")
    private String answer;

    private Integer score;

    private Integer sortOrder;
}
