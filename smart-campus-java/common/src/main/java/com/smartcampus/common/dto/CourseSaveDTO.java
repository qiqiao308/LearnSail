package com.smartcampus.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseSaveDTO {

    private Long id;

    @NotBlank(message = "课程标题不能为空")
    private String title;

    private String description;

    private String coverImage;

    @NotNull(message = "课程分类不能为空")
    private Long categoryId;

    private BigDecimal price;

    private String status;
}
