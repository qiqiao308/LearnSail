package com.smartcampus.admin.controller;

import com.smartcampus.admin.service.AdminAssignmentService;
import com.smartcampus.common.dto.AssignmentGradeDTO;
import com.smartcampus.common.dto.AssignmentSaveDTO;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.AssignmentSubmissionVO;
import com.smartcampus.common.vo.AssignmentVO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.common.vo.ResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/adminAssignment")
public class AdminAssignmentController {

    @Autowired
    private AdminAssignmentService adminAssignmentService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/loadAssignmentList")
    public ResponseVO<PageVO<AssignmentVO>> loadAssignmentList(@RequestParam(required = false) Long courseId,
                                                                @RequestBody PageDTO pageDTO) {
        PageVO<AssignmentVO> result = adminAssignmentService.loadAssignmentList(courseId, pageDTO);
        return ResponseVO.success(result);
    }

    @PostMapping("/saveAssignment")
    public ResponseVO<Void> saveAssignment(@RequestBody @Valid AssignmentSaveDTO dto) {
        adminAssignmentService.saveAssignment(dto);
        return ResponseVO.success(null);
    }

    @DeleteMapping("/deleteAssignment")
    public ResponseVO<Void> deleteAssignment(@RequestParam Long assignmentId) {
        adminAssignmentService.deleteAssignment(assignmentId);
        return ResponseVO.success(null);
    }

    @PostMapping("/loadSubmissionList")
    public ResponseVO<PageVO<AssignmentSubmissionVO>> loadSubmissionList(@RequestParam Long assignmentId,
                                                                          @RequestBody PageDTO pageDTO) {
        PageVO<AssignmentSubmissionVO> result = adminAssignmentService.loadSubmissionList(assignmentId, pageDTO);
        return ResponseVO.success(result);
    }

    @PutMapping("/gradeSubmission")
    public ResponseVO<Void> gradeSubmission(@RequestBody @Valid AssignmentGradeDTO dto) {
        adminAssignmentService.gradeSubmission(dto);
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
