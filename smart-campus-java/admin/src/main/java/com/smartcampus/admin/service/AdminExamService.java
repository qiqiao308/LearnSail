package com.smartcampus.admin.service;

import com.smartcampus.common.dto.ExamSaveDTO;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.dto.QuestionSaveDTO;
import com.smartcampus.common.vo.ExamRecordVO;
import com.smartcampus.common.vo.ExamVO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.common.vo.QuestionVO;

import java.util.List;

public interface AdminExamService {

    PageVO<ExamVO> loadExamList(Long courseId, PageDTO pageDTO);

    void saveExam(ExamSaveDTO dto);

    void deleteExam(Long examId);

    List<QuestionVO> loadQuestionList(Long examId);

    void saveQuestion(QuestionSaveDTO dto);

    void deleteQuestion(Long questionId);

    PageVO<ExamRecordVO> loadExamRecords(Long examId, PageDTO pageDTO);

    void gradeShortAnswer(Long questionRecordId, Integer score);
}
