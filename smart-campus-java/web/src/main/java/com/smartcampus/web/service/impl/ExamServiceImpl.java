package com.smartcampus.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.common.dto.AnswerDTO;
import com.smartcampus.common.dto.ExamSubmitDTO;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.enums.QuestionTypeEnum;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.mapper.CourseMapper;
import com.smartcampus.common.mapper.ExamMapper;
import com.smartcampus.common.mapper.ExamQuestionRecordMapper;
import com.smartcampus.common.mapper.ExamRecordMapper;
import com.smartcampus.common.mapper.QuestionMapper;
import com.smartcampus.common.po.Course;
import com.smartcampus.common.po.Exam;
import com.smartcampus.common.po.ExamQuestionRecord;
import com.smartcampus.common.po.ExamRecord;
import com.smartcampus.common.po.Question;
import com.smartcampus.common.vo.ExamDetailVO;
import com.smartcampus.common.vo.ExamResultVO;
import com.smartcampus.common.vo.ExamVO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.common.vo.QuestionRecordVO;
import com.smartcampus.common.vo.QuestionVO;
import com.smartcampus.web.service.ExamService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private ExamQuestionRecordMapper examQuestionRecordMapper;

    @Autowired
    private CourseMapper courseMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageVO<ExamVO> loadExamList(Long userId, Long courseId, PageDTO pageDTO) {
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Exam::getCourseId, courseId);
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

            // course title
            Course course = courseMapper.selectById(exam.getCourseId());
            if (course != null) {
                vo.setCourseTitle(course.getTitle());
            }

            // question count
            LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
            questionWrapper.eq(Question::getExamId, exam.getId());
            vo.setQuestionCount(questionMapper.selectCount(questionWrapper).intValue());

            // my status and score
            LambdaQueryWrapper<ExamRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(ExamRecord::getExamId, exam.getId());
            recordWrapper.eq(ExamRecord::getUserId, userId);
            ExamRecord record = examRecordMapper.selectOne(recordWrapper);
            if (record != null) {
                if ("SUBMITTED".equals(record.getStatus())) {
                    vo.setMyStatus("已完成");
                } else {
                    vo.setMyStatus("进行中");
                }
                vo.setMyScore(record.getScore());
            }

            return vo;
        }).collect(Collectors.toList());

        PageVO<ExamVO> pageVO = new PageVO<>();
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

    @Override
    @Transactional
    public ExamDetailVO startExam(Long userId, Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException(400, "考试不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        if (exam.getStartTime() != null && now.isBefore(exam.getStartTime())) {
            throw new BusinessException(400, "考试尚未开始");
        }
        if (exam.getEndTime() != null && now.isAfter(exam.getEndTime())) {
            throw new BusinessException(400, "考试已结束");
        }

        // check existing record
        LambdaQueryWrapper<ExamRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(ExamRecord::getExamId, examId);
        recordWrapper.eq(ExamRecord::getUserId, userId);
        ExamRecord record = examRecordMapper.selectOne(recordWrapper);

        if (record != null && "SUBMITTED".equals(record.getStatus())) {
            throw new BusinessException(400, "您已完成该考试");
        }

        if (record == null) {
            record = new ExamRecord();
            record.setExamId(examId);
            record.setUserId(userId);
            record.setStatus("IN_PROGRESS");
            record.setStartTime(now);
            examRecordMapper.insert(record);
        }

        // query questions
        LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(Question::getExamId, examId);
        questionWrapper.orderByAsc(Question::getSortOrder);
        List<Question> questions = questionMapper.selectList(questionWrapper);

        // build ExamDetailVO
        ExamDetailVO vo = new ExamDetailVO();
        BeanUtils.copyProperties(exam, vo);
        if (exam.getStartTime() != null) {
            vo.setStartTime(exam.getStartTime().format(FORMATTER));
        }
        if (exam.getEndTime() != null) {
            vo.setEndTime(exam.getEndTime().format(FORMATTER));
        }
        vo.setCourseTitle(null);
        vo.setQuestionCount(questions.size());
        vo.setExamRecordId(record.getId());

        // build QuestionVO list (without answers)
        List<QuestionVO> questionVOList = questions.stream().map(question -> {
            QuestionVO questionVO = new QuestionVO();
            questionVO.setId(question.getId());
            questionVO.setQuestionType(question.getQuestionType());
            questionVO.setContent(question.getContent());
            questionVO.setOptions(question.getOptions());
            questionVO.setScore(question.getScore());
            questionVO.setSortOrder(question.getSortOrder());
            return questionVO;
        }).collect(Collectors.toList());

        vo.setQuestions(questionVOList);
        return vo;
    }

    @Override
    @Transactional
    public void submitExam(Long userId, ExamSubmitDTO dto) {
        ExamRecord record = examRecordMapper.selectById(dto.getExamRecordId());
        if (record == null) {
            throw new BusinessException(400, "考试记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(400, "无权操作");
        }
        if ("SUBMITTED".equals(record.getStatus())) {
            throw new BusinessException(400, "已提交过");
        }

        Exam exam = examMapper.selectById(record.getExamId());

        int totalScore = 0;
        if (dto.getAnswers() != null) {
            for (AnswerDTO answer : dto.getAnswers()) {
                Question question = questionMapper.selectById(answer.getQuestionId());
                if (question == null) {
                    continue;
                }

                ExamQuestionRecord questionRecord = new ExamQuestionRecord();
                questionRecord.setExamRecordId(record.getId());
                questionRecord.setQuestionId(answer.getQuestionId());
                questionRecord.setUserAnswer(answer.getUserAnswer());
                questionRecord.setQuestionType(question.getQuestionType());

                // determine correctness
                String questionType = question.getQuestionType();
                boolean correct = false;
                if (QuestionTypeEnum.SINGLE_CHOICE.getCode().equals(questionType)
                        || QuestionTypeEnum.TRUE_FALSE.getCode().equals(questionType)
                        || QuestionTypeEnum.MULTIPLE_CHOICE.getCode().equals(questionType)) {
                    // auto-grade
                    if (answer.getUserAnswer() != null && answer.getUserAnswer().equals(question.getAnswer())) {
                        correct = true;
                        questionRecord.setIsCorrect(1);
                        questionRecord.setScore(question.getScore());
                        totalScore += question.getScore();
                    } else {
                        questionRecord.setIsCorrect(0);
                        questionRecord.setScore(0);
                    }
                } else {
                    // SHORT_ANSWER: teacher grades later
                    questionRecord.setIsCorrect(0);
                    questionRecord.setScore(0);
                }

                examQuestionRecordMapper.insert(questionRecord);
            }
        }

        record.setScore(totalScore);
        record.setStatus("SUBMITTED");
        record.setSubmitTime(LocalDateTime.now());
        examRecordMapper.updateById(record);
    }

    @Override
    public ExamResultVO getExamScore(Long userId, Long examRecordId) {
        ExamRecord record = examRecordMapper.selectById(examRecordId);
        if (record == null) {
            throw new BusinessException(400, "考试记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(400, "无权查看");
        }

        Exam exam = examMapper.selectById(record.getExamId());

        ExamResultVO vo = new ExamResultVO();
        vo.setExamRecordId(record.getId());
        if (exam != null) {
            vo.setExamTitle(exam.getTitle());
            vo.setTotalScore(exam.getTotalScore());
            vo.setPassScore(exam.getPassScore());
            if (record.getScore() != null && exam.getPassScore() != null) {
                vo.setIsPassed(record.getScore() >= exam.getPassScore());
            } else {
                vo.setIsPassed(false);
            }
        }
        vo.setMyScore(record.getScore());
        vo.setStatus(record.getStatus());
        if (record.getStartTime() != null) {
            vo.setStartTime(record.getStartTime().format(FORMATTER));
        }
        if (record.getSubmitTime() != null) {
            vo.setSubmitTime(record.getSubmitTime().format(FORMATTER));
        }

        // query question records
        LambdaQueryWrapper<ExamQuestionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamQuestionRecord::getExamRecordId, examRecordId);
        List<ExamQuestionRecord> questionRecords = examQuestionRecordMapper.selectList(wrapper);

        List<QuestionRecordVO> questionRecordVOList = new ArrayList<>();
        for (ExamQuestionRecord qr : questionRecords) {
            Question question = questionMapper.selectById(qr.getQuestionId());
            if (question == null) {
                continue;
            }
            QuestionRecordVO qrVO = new QuestionRecordVO();
            qrVO.setQuestionId(qr.getQuestionId());
            qrVO.setQuestionType(qr.getQuestionType());
            qrVO.setContent(question.getContent());
            qrVO.setOptions(question.getOptions());
            qrVO.setUserAnswer(qr.getUserAnswer());
            qrVO.setCorrectAnswer(question.getAnswer());
            qrVO.setIsCorrect(qr.getIsCorrect() != null && qr.getIsCorrect() == 1);
            qrVO.setScore(qr.getScore());
            qrVO.setTotalQuestionScore(question.getScore());
            questionRecordVOList.add(qrVO);
        }
        vo.setQuestionRecords(questionRecordVOList);

        return vo;
    }
}