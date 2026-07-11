package com.smartcampus.web.controller;

import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.ResponseVO;
import com.smartcampus.web.service.ProgressService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/progress")
public class ProgressController {

    @Autowired
    private ProgressService progressService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/markSectionComplete")
    public ResponseVO<Void> markSectionComplete(@RequestParam Long sectionId, @RequestParam Long courseId,
                                                 HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        progressService.markSectionComplete(userId, sectionId, courseId);
        return ResponseVO.success("标记完成成功", null);
    }

    @GetMapping("/getCourseProgress")
    public ResponseVO<Double> getCourseProgress(@RequestParam Long courseId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return ResponseVO.success(progressService.getCourseProgress(userId, courseId));
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
