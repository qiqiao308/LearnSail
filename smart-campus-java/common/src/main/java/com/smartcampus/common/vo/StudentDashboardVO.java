package com.smartcampus.common.vo;

import lombok.Data;

import java.util.List;

@Data
public class StudentDashboardVO {

    private Long enrolledCourseCount;

    private Long completedCourseCount;

    private Long totalLearningMinutes;

    private Double averageScore;

    private List<EnrollmentVO> recentEnrollments;
}
