package com.smartcampus.web.controller;

import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.ResponseVO;
import com.smartcampus.common.vo.StudentDashboardVO;
import com.smartcampus.web.service.StudentStatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/studentStat")
public class StudentStatController {

    @Autowired
    private StudentStatService studentStatService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/getMyDashboard")
    public ResponseVO<StudentDashboardVO> getMyDashboard(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return ResponseVO.success(studentStatService.getMyDashboard(userId));
    }

    @GetMapping("/getLearningStats")
    public ResponseVO<StudentDashboardVO> getLearningStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return ResponseVO.success(studentStatService.getMyDashboard(userId));
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserId(token);
        }
        return null;
    }
}
