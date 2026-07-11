package com.smartcampus.web.service;

import com.smartcampus.common.vo.StudentDashboardVO;

public interface StudentStatService {
    StudentDashboardVO getMyDashboard(Long userId);
}
