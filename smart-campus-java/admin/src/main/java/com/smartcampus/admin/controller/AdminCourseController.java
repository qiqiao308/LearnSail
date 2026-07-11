package com.smartcampus.admin.controller;

import com.smartcampus.admin.service.AdminCourseService;
import com.smartcampus.common.dto.CourseQueryDTO;
import com.smartcampus.common.dto.CourseSaveDTO;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.CourseVO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.common.vo.ResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/adminCourse")
public class AdminCourseController {

    @Autowired
    private AdminCourseService adminCourseService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/loadCourseList")
    public ResponseVO<PageVO<CourseVO>> loadCourseList(@RequestBody CourseQueryDTO queryDTO) {
        PageVO<CourseVO> result = adminCourseService.loadCourseList(queryDTO);
        return ResponseVO.success(result);
    }

    @PostMapping("/saveCourse")
    public ResponseVO<Void> saveCourse(@RequestBody @Valid CourseSaveDTO dto) {
        adminCourseService.saveCourse(dto);
        return ResponseVO.success(null);
    }

    @PutMapping("/updateCourse")
    public ResponseVO<Void> updateCourse(@RequestBody CourseSaveDTO dto) {
        adminCourseService.updateCourse(dto);
        return ResponseVO.success(null);
    }

    @DeleteMapping("/deleteCourse")
    public ResponseVO<Void> deleteCourse(@RequestParam Long courseId) {
        adminCourseService.deleteCourse(courseId);
        return ResponseVO.success(null);
    }

    @PutMapping("/publishCourse")
    public ResponseVO<Void> publishCourse(@RequestParam Long courseId) {
        adminCourseService.publishCourse(courseId);
        return ResponseVO.success(null);
    }

    @PutMapping("/closeCourse")
    public ResponseVO<Void> closeCourse(@RequestParam Long courseId) {
        adminCourseService.closeCourse(courseId);
        return ResponseVO.success(null);
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
