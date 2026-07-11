package com.smartcampus.web.controller;

import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.EnrollmentVO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.common.vo.ResponseVO;
import com.smartcampus.web.service.EnrollmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enrollment")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/enrollCourse")
    public ResponseVO<Void> enrollCourse(@RequestParam Long courseId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        enrollmentService.enrollCourse(userId, courseId);
        return ResponseVO.success("选课成功", null);
    }

    @PostMapping("/dropCourse")
    public ResponseVO<Void> dropCourse(@RequestParam Long courseId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        enrollmentService.dropCourse(userId, courseId);
        return ResponseVO.success("退课成功", null);
    }

    @PostMapping("/loadMyEnrollments")
    public ResponseVO<PageVO<EnrollmentVO>> loadMyEnrollments(@RequestBody PageDTO pageDTO, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return ResponseVO.success(enrollmentService.loadMyEnrollments(userId, pageDTO));
    }

    @PostMapping("/addFavorite")
    public ResponseVO<Void> addFavorite(@RequestParam Long courseId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        enrollmentService.addFavorite(userId, courseId);
        return ResponseVO.success("收藏成功", null);
    }

    @PostMapping("/removeFavorite")
    public ResponseVO<Void> removeFavorite(@RequestParam Long courseId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        enrollmentService.removeFavorite(userId, courseId);
        return ResponseVO.success("取消收藏成功", null);
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
