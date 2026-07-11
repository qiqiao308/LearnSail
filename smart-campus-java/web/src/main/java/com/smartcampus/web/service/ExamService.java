package com.smartcampus.web.service;

import com.smartcampus.common.dto.ExamSubmitDTO;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.vo.ExamDetailVO;
import com.smartcampus.common.vo.ExamResultVO;
import com.smartcampus.common.vo.ExamVO;
import com.smartcampus.common.vo.PageVO;

public interface ExamService {
    PageVO<ExamVO> loadExamList(Long userId, Long courseId, PageDTO pageDTO);
    ExamDetailVO startExam(Long userId, Long examId);
    void submitExam(Long userId, ExamSubmitDTO dto);
    ExamResultVO getExamScore(Long userId, Long examRecordId);
}
