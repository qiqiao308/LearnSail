package com.smartcampus.admin.controller;

import com.smartcampus.admin.service.AdminExamService;
import com.smartcampus.common.dto.ExamSaveDTO;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.dto.QuestionSaveDTO;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adminExam")
public class AdminExamController {

    @Autowired
    private AdminExamService adminExamService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/loadExamList")
    public ResponseVO<PageVO<ExamVO>> loadExamList(@RequestParam(required = false) Long courseId,
                                                    @RequestBody PageDTO pageDTO) {
        PageVO<ExamVO> result = adminExamService.loadExamList(courseId, pageDTO);
        return ResponseVO.success(result);
    }

    @PostMapping("/saveExam")
    public ResponseVO<Void> saveExam(@RequestBody @Valid ExamSaveDTO dto) {
        adminExamService.saveExam(dto);
        return ResponseVO.success(null);
    }

    @DeleteMapping("/deleteExam")
    public ResponseVO<Void> deleteExam(@RequestParam Long examId) {
        adminExamService.deleteExam(examId);
        return ResponseVO.success(null);
    }

    @GetMapping("/loadQuestionList")
    public ResponseVO<List<QuestionVO>> loadQuestionList(@RequestParam Long examId) {
        List<QuestionVO> result = adminExamService.loadQuestionList(examId);
        return ResponseVO.success(result);
    }

    @PostMapping("/saveQuestion")
    public ResponseVO<Void> saveQuestion(@RequestBody @Valid QuestionSaveDTO dto) {
        adminExamService.saveQuestion(dto);
        return ResponseVO.success(null);
    }

    @DeleteMapping("/deleteQuestion")
    public ResponseVO<Void> deleteQuestion(@RequestParam Long questionId) {
        adminExamService.deleteQuestion(questionId);
        return ResponseVO.success(null);
    }

    @PostMapping("/loadExamRecords")
    public ResponseVO<PageVO<ExamRecordVO>> loadExamRecords(@RequestParam Long examId,
                                                             @RequestBody PageDTO pageDTO) {
        PageVO<ExamRecordVO> result = adminExamService.loadExamRecords(examId, pageDTO);
        return ResponseVO.success(result);
    }

    @PutMapping("/gradeShortAnswer")
    public ResponseVO<Void> gradeShortAnswer(@RequestParam Long questionRecordId,
                                              @RequestParam Integer score) {
        adminExamService.gradeShortAnswer(questionRecordId, score);
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
