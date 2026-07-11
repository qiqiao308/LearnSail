package com.smartcampus.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignmentSubmitDTO {

    @NotNull(message = "作业ID不能为空")
    private Long assignmentId;

    private String content;

    private String fileUrl;
}
