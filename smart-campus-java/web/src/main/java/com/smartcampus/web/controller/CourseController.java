package com.smartcampus.web.controller;

import com.smartcampus.common.dto.CourseQueryDTO;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.ChapterVO;
import com.smartcampus.common.vo.CourseDetailVO;
import com.smartcampus.common.vo.CourseVO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.common.vo.ResponseVO;
import com.smartcampus.web.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/courseInfo")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/loadCourseInfoList")
    public ResponseVO<PageVO<CourseVO>> loadCourseInfoList(@RequestBody CourseQueryDTO queryDTO) {
        return ResponseVO.success(courseService.loadCourseInfoList(queryDTO));
    }

    @GetMapping("/getCourseInfo")
    public ResponseVO<CourseDetailVO> getCourseInfo(@RequestParam Long courseId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return ResponseVO.success(courseService.getCourseInfo(courseId, userId));
    }

    @GetMapping("/loadChapterList")
    public ResponseVO<List<ChapterVO>> loadChapterList(@RequestParam Long courseId) {
        return ResponseVO.success(courseService.loadChapterList(courseId));
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
