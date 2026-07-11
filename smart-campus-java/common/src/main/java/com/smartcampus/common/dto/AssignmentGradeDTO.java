package com.smartcampus.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignmentGradeDTO {

    @NotNull(message = "提交记录ID不能为空")
    private Long submissionId;

    @NotNull(message = "分数不能为空")
    private Integer score;

    private String comment;
}
