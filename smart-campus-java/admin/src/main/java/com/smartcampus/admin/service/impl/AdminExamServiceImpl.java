package com.smartcampus.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.admin.service.AdminExamService;
import com.smartcampus.common.dto.ExamSaveDTO;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.dto.QuestionSaveDTO;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.mapper.*;
import com.smartcampus.common.po.*;
import com.smartcampus.common.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminExamServiceImpl implements AdminExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private ExamQuestionRecordMapper examQuestionRecordMapper;

    @Autowired
    private UserMapper userMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageVO<ExamVO> loadExamList(Long courseId, PageDTO pageDTO) {
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(Exam::getCourseId, courseId);
        }
        wrapper.orderByDesc(Exam::getCreateTime);

        Page<Exam> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        Page<Exam> result = examMapper.selectPage(page, wrapper);

        List<ExamVO> voList = result.getRecords().stream().map(exam -> {
            ExamVO vo = new ExamVO();
            BeanUtils.copyProperties(exam, vo);
            if (exam.getStartTime() != null) {
                vo.setStartTime(exam.getStartTime().format(FORMATTER));
            }
            if (exam.getEndTime() != null) {
                vo.setEndTime(exam.getEndTime().format(FORMATTER));
            }
            // count questions
            LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
            questionWrapper.eq(Question::getExamId, exam.getId());
            vo.setQuestionCount(questionMapper.selectCount(questionWrapper).intValue());
            return vo;
        }).collect(Collectors.toList());

        PageVO<ExamVO> pageVO = new PageVO<>();
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

    @Override
    public void saveExam(ExamSaveDTO dto) {
        Exam exam = new Exam();
        BeanUtils.copyProperties(dto, exam);
        if (dto.getStartTime() != null && !dto.getStartTime().isBlank()) {
            exam.setStartTime(LocalDateTime.parse(dto.getStartTime(), FORMATTER));
        }
        if (dto.getEndTime() != null && !dto.getEndTime().isBlank()) {
            exam.setEndTime(LocalDateTime.parse(dto.getEndTime(), FORMATTER));
        }
        exam.setCreateTime(LocalDateTime.now());
        exam.setStatus("NOT_STARTED");
        examMapper.insert(exam);
    }

    @Override
    @Transactional
    public void deleteExam(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException(404, "考试不存在");
        }
        // delete all questions
        LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(Question::getExamId, examId);
        questionMapper.delete(questionWrapper);
        // delete exam itself
        examMapper.deleteById(examId);
    }

    @Override
    public List<QuestionVO> loadQuestionList(Long examId) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getExamId, examId);
        wrapper.orderByAsc(Question::getSortOrder);
        List<Question> questions = questionMapper.selectList(wrapper);

        return questions.stream().map(question -> {
            QuestionVO vo = new QuestionVO();
            BeanUtils.copyProperties(question, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void saveQuestion(QuestionSaveDTO dto) {
        Question question = new Question();
        BeanUtils.copyProperties(dto, question);
        questionMapper.insert(question);
    }

    @Override
    public void deleteQuestion(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException(404, "题目不存在");
        }
        questionMapper.deleteById(questionId);
    }

    @Override
    public PageVO<ExamRecordVO> loadExamRecords(Long examId, PageDTO pageDTO) {
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getExamId, examId);
        wrapper.orderByDesc(ExamRecord::getSubmitTime);

        Page<ExamRecord> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        Page<ExamRecord> result = examRecordMapper.selectPage(page, wrapper);

        List<ExamRecordVO> voList = result.getRecords().stream().map(record -> {
            ExamRecordVO vo = new ExamRecordVO();
            BeanUtils.copyProperties(record, vo);
            if (record.getStartTime() != null) {
                vo.setStartTime(record.getStartTime().format(FORMATTER));
            }
            if (record.getSubmitTime() != null) {
                vo.setSubmitTime(record.getSubmitTime().format(FORMATTER));
            }
            // resolve student name
            User student = userMapper.selectById(record.getUserId());
            if (student != null) {
                vo.setStudentName(student.getRealName());
            }
            return vo;
        }).collect(Collectors.toList());

        PageVO<ExamRecordVO> pageVO = new PageVO<>();
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

    @Override
    @Transactional
    public void gradeShortAnswer(Long questionRecordId, Integer score) {
        ExamQuestionRecord questionRecord = examQuestionRecordMapper.selectById(questionRecordId);
        if (questionRecord == null) {
            throw new BusinessException(404, "答题记录不存在");
        }
        questionRecord.setScore(score);
        questionRecord.setIsCorrect(1);
        examQuestionRecordMapper.updateById(questionRecord);

        // recalculate exam_record total score
        Long examRecordId = questionRecord.getExamRecordId();
        LambdaQueryWrapper<ExamQuestionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamQuestionRecord::getExamRecordId, examRecordId);
        List<ExamQuestionRecord> allRecords = examQuestionRecordMapper.selectList(wrapper);
        int totalScore = allRecords.stream().mapToInt(r -> r.getScore() != null ? r.getScore() : 0).sum();

        ExamRecord examRecord = examRecordMapper.selectById(examRecordId);
        if (examRecord != null) {
            examRecord.setScore(totalScore);
            examRecord.setStatus("GRADED");
            examRecordMapper.updateById(examRecord);
        }
    }
}