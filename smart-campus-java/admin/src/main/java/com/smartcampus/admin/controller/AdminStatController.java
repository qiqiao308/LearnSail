package com.smartcampus.admin.controller;

import com.smartcampus.admin.service.AdminStatService;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.DashboardStatsVO;
import com.smartcampus.common.vo.ResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/adminStat")
public class AdminStatController {

    @Autowired
    private AdminStatService adminStatService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/getDashboardStats")
    public ResponseVO<DashboardStatsVO> getDashboardStats() {
        DashboardStatsVO result = adminStatService.getDashboardStats();
        return ResponseVO.success(result);
    }

    @GetMapping("/getUserStats")
    public ResponseVO<Map<String, Object>> getUserStats() {
        Map<String, Object> result = adminStatService.getUserStats();
        return ResponseVO.success(result);
    }

    @GetMapping("/getCourseStats")
    public ResponseVO<Map<String, Object>> getCourseStats() {
        Map<String, Object> result = adminStatService.getCourseStats();
        return ResponseVO.success(result);
    }

    @GetMapping("/getDailyActivity")
    public ResponseVO<Map<String, Object>> getDailyActivity() {
        Map<String, Object> result = adminStatService.getDailyActivity();
        return ResponseVO.success(result);
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
