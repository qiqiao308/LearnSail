package com.smartcampus.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SectionSaveDTO {

    private Long id;

    @NotNull(message = "章节ID不能为空")
    private Long chapterId;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotBlank(message = "小节标题不能为空")
    private String title;

    private String contentType;

    private String contentUrl;

    private String contentText;

    private Integer duration;

    private Integer sortOrder;
}
