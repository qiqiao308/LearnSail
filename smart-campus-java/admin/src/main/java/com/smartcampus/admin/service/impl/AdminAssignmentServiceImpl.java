package com.smartcampus.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.admin.service.AdminAssignmentService;
import com.smartcampus.common.dto.AssignmentGradeDTO;
import com.smartcampus.common.dto.AssignmentSaveDTO;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.mapper.AssignmentMapper;
import com.smartcampus.common.mapper.AssignmentSubmissionMapper;
import com.smartcampus.common.mapper.UserMapper;
import com.smartcampus.common.po.Assignment;
import com.smartcampus.common.po.AssignmentSubmission;
import com.smartcampus.common.po.User;
import com.smartcampus.common.vo.AssignmentSubmissionVO;
import com.smartcampus.common.vo.AssignmentVO;
import com.smartcampus.common.vo.PageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminAssignmentServiceImpl implements AdminAssignmentService {

    @Autowired
    private AssignmentMapper assignmentMapper;

    @Autowired
    private AssignmentSubmissionMapper assignmentSubmissionMapper;

    @Autowired
    private UserMapper userMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageVO<AssignmentVO> loadAssignmentList(Long courseId, PageDTO pageDTO) {
        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(Assignment::getCourseId, courseId);
        }
        wrapper.orderByDesc(Assignment::getCreateTime);

        Page<Assignment> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        Page<Assignment> result = assignmentMapper.selectPage(page, wrapper);

        List<AssignmentVO> voList = result.getRecords().stream().map(assignment -> {
            AssignmentVO vo = new AssignmentVO();
            BeanUtils.copyProperties(assignment, vo);
            if (assignment.getCreateTime() != null) {
                vo.setCreateTime(assignment.getCreateTime().format(FORMATTER));
            }
            if (assignment.getDeadline() != null) {
                vo.setDeadline(assignment.getDeadline().format(FORMATTER));
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
    public void saveAssignment(AssignmentSaveDTO dto) {
        Assignment assignment = new Assignment();
        BeanUtils.copyProperties(dto, assignment);
        if (dto.getDeadline() != null && !dto.getDeadline().isBlank()) {
            assignment.setDeadline(LocalDateTime.parse(dto.getDeadline(), FORMATTER));
        }
        assignment.setCreateTime(LocalDateTime.now());
        assignmentMapper.insert(assignment);
    }

    @Override
    public void deleteAssignment(Long assignmentId) {
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new BusinessException(404, "作业不存在");
        }
        assignmentMapper.deleteById(assignmentId);
    }

    @Override
    public PageVO<AssignmentSubmissionVO> loadSubmissionList(Long assignmentId, PageDTO pageDTO) {
        LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssignmentSubmission::getAssignmentId, assignmentId);
        wrapper.orderByDesc(AssignmentSubmission::getSubmitTime);

        Page<AssignmentSubmission> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        Page<AssignmentSubmission> result = assignmentSubmissionMapper.selectPage(page, wrapper);

        List<AssignmentSubmissionVO> voList = result.getRecords().stream().map(submission -> {
            AssignmentSubmissionVO vo = new AssignmentSubmissionVO();
            BeanUtils.copyProperties(submission, vo);
            if (submission.getSubmitTime() != null) {
                vo.setSubmitTime(submission.getSubmitTime().format(FORMATTER));
            }
            if (submission.getGradeTime() != null) {
                vo.setGradeTime(submission.getGradeTime().format(FORMATTER));
            }
            // resolve student name
            if (submission.getUserId() != null) {
                User student = userMapper.selectById(submission.getUserId());
                if (student != null) {
                    vo.setStudentName(student.getRealName());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        PageVO<AssignmentSubmissionVO> pageVO = new PageVO<>();
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

    @Override
    public void gradeSubmission(AssignmentGradeDTO dto) {
        AssignmentSubmission submission = assignmentSubmissionMapper.selectById(dto.getSubmissionId());
        if (submission == null) {
            throw new BusinessException(404, "提交记录不存在");
        }
        submission.setScore(dto.getScore());
        submission.setComment(dto.getComment());
        submission.setGradeTime(LocalDateTime.now());
        assignmentSubmissionMapper.updateById(submission);
    }
}
