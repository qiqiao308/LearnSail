package com.smartcampus.web.controller;

import com.smartcampus.common.dto.ExamSubmitDTO;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.ExamDetailVO;
import com.smartcampus.common.vo.ExamResultVO;
import com.smartcampus.common.vo.ExamVO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.common.vo.ResponseVO;
import com.smartcampus.web.service.ExamService;
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
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/loadExamList")
    public ResponseVO<PageVO<ExamVO>> loadExamList(@RequestParam Long courseId,
                                                    @RequestBody PageDTO pageDTO,
                                                    HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return ResponseVO.success(examService.loadExamList(userId, courseId, pageDTO));
    }

    @PostMapping("/startExam")
    public ResponseVO<ExamDetailVO> startExam(@RequestParam Long examId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return ResponseVO.success(examService.startExam(userId, examId));
    }

    @PostMapping("/submitExam")
    public ResponseVO<Void> submitExam(@RequestBody @Valid ExamSubmitDTO dto, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        examService.submitExam(userId, dto);
        return ResponseVO.success("提交成功", null);
    }

    @GetMapping("/getExamScore")
    public ResponseVO<ExamResultVO> getExamScore(@RequestParam Long examRecordId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return ResponseVO.success(examService.getExamScore(userId, examRecordId));
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
