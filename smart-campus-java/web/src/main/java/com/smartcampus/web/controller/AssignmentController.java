package com.smartcampus.web.controller;

import com.smartcampus.common.dto.AssignmentSubmitDTO;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.AssignmentSubmissionVO;
import com.smartcampus.common.vo.AssignmentVO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.common.vo.ResponseVO;
import com.smartcampus.web.service.AssignmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assignment")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/loadAssignmentList")
    public ResponseVO<PageVO<AssignmentVO>> loadAssignmentList(@RequestParam Long courseId,
                                                                @RequestBody PageDTO pageDTO,
                                                                HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return ResponseVO.success(assignmentService.loadAssignmentList(userId, courseId, pageDTO));
    }

    @PostMapping("/submitAssignment")
    public ResponseVO<Void> submitAssignment(@RequestBody @Valid AssignmentSubmitDTO dto, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        assignmentService.submitAssignment(userId, dto);
        return ResponseVO.success("提交成功", null);
    }

    @GetMapping("/getMySubmission")
    public ResponseVO<AssignmentSubmissionVO> getMySubmission(@RequestParam Long assignmentId,
                                                               HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return ResponseVO.success(assignmentService.getMySubmission(userId, assignmentId));
    }

    @GetMapping("/getGrade")
    public ResponseVO<AssignmentSubmissionVO> getGrade(@RequestParam Long assignmentId,
                                                        HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return ResponseVO.success(assignmentService.getGrade(userId, assignmentId));
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
