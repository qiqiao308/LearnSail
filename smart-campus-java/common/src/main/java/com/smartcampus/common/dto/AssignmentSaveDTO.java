package com.smartcampus.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignmentSaveDTO {

    private Long id;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    private Long chapterId;

    @NotBlank(message = "作业标题不能为空")
    private String title;

    private String description;

    private String deadline;

    private Integer maxScore;
}
