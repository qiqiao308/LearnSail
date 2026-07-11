package com.smartcampus.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.common.dto.AssignmentSubmitDTO;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.mapper.AssignmentMapper;
import com.smartcampus.common.mapper.AssignmentSubmissionMapper;
import com.smartcampus.common.mapper.CourseMapper;
import com.smartcampus.common.mapper.UserMapper;
import com.smartcampus.common.po.Assignment;
import com.smartcampus.common.po.AssignmentSubmission;
import com.smartcampus.common.po.Course;
import com.smartcampus.common.po.User;
import com.smartcampus.common.vo.AssignmentSubmissionVO;
import com.smartcampus.common.vo.AssignmentVO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.web.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    @Autowired
    private AssignmentMapper assignmentMapper;

    @Autowired
    private AssignmentSubmissionMapper assignmentSubmissionMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageVO<AssignmentVO> loadAssignmentList(Long userId, Long courseId, PageDTO pageDTO) {
        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Assignment::getCourseId, courseId)
                .orderByDesc(Assignment::getCreateTime);

        Page<Assignment> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        Page<Assignment> result = assignmentMapper.selectPage(page, wrapper);

        List<AssignmentVO> voList = result.getRecords().stream().map(assignment -> {
            AssignmentVO vo = new AssignmentVO();
            vo.setId(assignment.getId());
            vo.setCourseId(assignment.getCourseId());
            vo.setChapterId(assignment.getChapterId());
            vo.setTitle(assignment.getTitle());
            vo.setDescription(assignment.getDescription());
            vo.setMaxScore(assignment.getMaxScore());
            if (assignment.getDeadline() != null) {
                vo.setDeadline(assignment.getDeadline().toString());
            }
            if (assignment.getCreateTime() != null) {
                vo.setCreateTime(assignment.getCreateTime().toString());
            }

            // get course title
            Course course = courseMapper.selectById(assignment.getCourseId());
            if (course != null) {
                vo.setCourseTitle(course.getTitle());
            }

            // check if user submitted
            LambdaQueryWrapper<AssignmentSubmission> submissionWrapper = new LambdaQueryWrapper<>();
            submissionWrapper.eq(AssignmentSubmission::getAssignmentId, assignment.getId())
                    .eq(AssignmentSubmission::getUserId, userId);
            long submissionCount = assignmentSubmissionMapper.selectCount(submissionWrapper);
            if (submissionCount > 0) {
                vo.setIsSubmitted(true);
                AssignmentSubmission submission = assignmentSubmissionMapper.selectOne(submissionWrapper);
                if (submission != null) {
                    vo.setMyScore(submission.getScore());
                    vo.setSubmissionStatus(submission.getScore() != null ? "GRADED" : "SUBMITTED");
                }
            } else {
                vo.setIsSubmitted(false);
                vo.setSubmissionStatus("NOT_SUBMITTED");
            }

            return vo;
        }).collect(Collectors.toList());

        PageVO<AssignmentVO> pageVO = new PageVO<>();
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

    @Override
    public void submitAssignment(Long userId, AssignmentSubmitDTO dto) {
        Assignment assignment = assignmentMapper.selectById(dto.getAssignmentId());
        if (assignment == null) {
            throw new BusinessException(400, "作业不存在");
        }
        if (assignment.getDeadline() != null && LocalDateTime.now().isAfter(assignment.getDeadline())) {
            throw new BusinessException(400, "已过提交截止时间");
        }

        LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssignmentSubmission::getAssignmentId, dto.getAssignmentId())
                .eq(AssignmentSubmission::getUserId, userId);
        if (assignmentSubmissionMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "已提交过该作业");
        }

        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setAssignmentId(dto.getAssignmentId());
        submission.setUserId(userId);
        submission.setContent(dto.getContent());
        submission.setFileUrl(dto.getFileUrl());
        submission.setSubmitTime(LocalDateTime.now());
        assignmentSubmissionMapper.insert(submission);
    }

    @Override
    public AssignmentSubmissionVO getMySubmission(Long userId, Long assignmentId) {
        LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssignmentSubmission::getAssignmentId, assignmentId)
                .eq(AssignmentSubmission::getUserId, userId);
        AssignmentSubmission submission = assignmentSubmissionMapper.selectOne(wrapper);
        if (submission == null) {
            return null;
        }
        return buildSubmissionVO(submission);
    }

    @Override
    public AssignmentSubmissionVO getGrade(Long userId, Long assignmentId) {
        LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssignmentSubmission::getAssignmentId, assignmentId)
                .eq(AssignmentSubmission::getUserId, userId);
        AssignmentSubmission submission = assignmentSubmissionMapper.selectOne(wrapper);
        if (submission == null) {
            return null;
        }
        return buildSubmissionVO(submission);
    }

    private AssignmentSubmissionVO buildSubmissionVO(AssignmentSubmission submission) {
        AssignmentSubmissionVO vo = new AssignmentSubmissionVO();
        vo.setId(submission.getId());
        vo.setAssignmentId(submission.getAssignmentId());
        vo.setUserId(submission.getUserId());
        vo.setContent(submission.getContent());
        vo.setFileUrl(submission.getFileUrl());
        vo.setScore(submission.getScore());
        vo.setComment(submission.getComment());
        if (submission.getSubmitTime() != null) {
            vo.setSubmitTime(submission.getSubmitTime().toString());
        }
        if (submission.getGradeTime() != null) {
            vo.setGradeTime(submission.getGradeTime().toString());
        }

        // get assignment title
        Assignment assignment = assignmentMapper.selectById(submission.getAssignmentId());
        if (assignment != null) {
            vo.setAssignmentTitle(assignment.getTitle());
        }

        // get student name
        User student = userMapper.selectById(submission.getUserId());
        if (student != null) {
            vo.setStudentName(student.getRealName());
        }

        return vo;
    }
}
