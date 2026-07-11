package com.smartcampus.web.service;

import com.smartcampus.common.dto.AssignmentSubmitDTO;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.vo.AssignmentSubmissionVO;
import com.smartcampus.common.vo.AssignmentVO;
import com.smartcampus.common.vo.PageVO;

public interface AssignmentService {
    PageVO<AssignmentVO> loadAssignmentList(Long userId, Long courseId, PageDTO pageDTO);
    void submitAssignment(Long userId, AssignmentSubmitDTO dto);
    AssignmentSubmissionVO getMySubmission(Long userId, Long assignmentId);
    AssignmentSubmissionVO getGrade(Long userId, Long assignmentId);
}
