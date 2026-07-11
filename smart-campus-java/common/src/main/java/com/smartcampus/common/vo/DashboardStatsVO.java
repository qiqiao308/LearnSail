package com.smartcampus.common.vo;

import lombok.Data;

import java.util.List;

@Data
public class DashboardStatsVO {

    private Long totalUsers;

    private Long totalCourses;

    private Long totalEnrollments;

    private Long todayActiveUsers;

    private List<ChartDataVO> userGrowth;

    private List<ChartDataVO> coursePopularity;
}
