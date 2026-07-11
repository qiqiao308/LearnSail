package com.smartcampus.admin.service;

import com.smartcampus.common.dto.AssignmentGradeDTO;
import com.smartcampus.common.dto.AssignmentSaveDTO;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.vo.AssignmentSubmissionVO;
import com.smartcampus.common.vo.AssignmentVO;
import com.smartcampus.common.vo.PageVO;

public interface AdminAssignmentService {

    PageVO<AssignmentVO> loadAssignmentList(Long courseId, PageDTO pageDTO);

    void saveAssignment(AssignmentSaveDTO dto);

    void deleteAssignment(Long assignmentId);

    PageVO<AssignmentSubmissionVO> loadSubmissionList(Long assignmentId, PageDTO pageDTO);

    void gradeSubmission(AssignmentGradeDTO dto);
}
