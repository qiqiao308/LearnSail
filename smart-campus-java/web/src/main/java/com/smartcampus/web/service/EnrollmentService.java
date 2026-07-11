package com.smartcampus.web.service;

import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.vo.EnrollmentVO;
import com.smartcampus.common.vo.PageVO;

public interface EnrollmentService {
    void enrollCourse(Long userId, Long courseId);
    void dropCourse(Long userId, Long courseId);
    PageVO<EnrollmentVO> loadMyEnrollments(Long userId, PageDTO pageDTO);
    void addFavorite(Long userId, Long courseId);
    void removeFavorite(Long userId, Long courseId);
}
