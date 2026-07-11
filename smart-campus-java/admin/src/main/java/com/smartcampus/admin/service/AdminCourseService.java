package com.smartcampus.admin.service;

import com.smartcampus.common.dto.CourseQueryDTO;
import com.smartcampus.common.dto.CourseSaveDTO;
import com.smartcampus.common.vo.CourseVO;
import com.smartcampus.common.vo.PageVO;

public interface AdminCourseService {

    PageVO<CourseVO> loadCourseList(CourseQueryDTO queryDTO);

    void saveCourse(CourseSaveDTO dto);

    void updateCourse(CourseSaveDTO dto);

    void deleteCourse(Long courseId);

    void publishCourse(Long courseId);

    void closeCourse(Long courseId);
}
