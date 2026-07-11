package com.smartcampus.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CourseQueryDTO extends PageDTO {

    private String title;

    private Long categoryId;

    private String status;

    private Long teacherId;
}
