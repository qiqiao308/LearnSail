package com.smartcampus.common.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CourseDetailVO extends CourseVO {

    private List<ChapterVO> chapters;

    private Boolean isEnrolled;

    private Boolean isFavorite;

    private Double progress;
}
