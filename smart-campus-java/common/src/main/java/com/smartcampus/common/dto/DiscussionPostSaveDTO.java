package com.smartcampus.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DiscussionPostSaveDTO {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotBlank(message = "帖子标题不能为空")
    private String title;

    @NotBlank(message = "帖子内容不能为空")
    private String content;
}
