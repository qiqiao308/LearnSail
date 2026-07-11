package com.smartcampus.admin.service;

import com.smartcampus.common.vo.DashboardStatsVO;

import java.util.Map;

public interface AdminStatService {

    DashboardStatsVO getDashboardStats();

    Map<String, Object> getUserStats();

    Map<String, Object> getCourseStats();

    Map<String, Object> getDailyActivity();
}
