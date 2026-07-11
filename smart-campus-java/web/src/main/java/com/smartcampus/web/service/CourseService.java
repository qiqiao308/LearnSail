package com.smartcampus.web.service;

import com.smartcampus.common.dto.CourseQueryDTO;
import com.smartcampus.common.vo.ChapterVO;
import com.smartcampus.common.vo.CourseDetailVO;
import com.smartcampus.common.vo.CourseVO;
import com.smartcampus.common.vo.PageVO;

import java.util.List;

public interface CourseService {
    PageVO<CourseVO> loadCourseInfoList(CourseQueryDTO queryDTO);
    CourseDetailVO getCourseInfo(Long courseId, Long userId);
    List<ChapterVO> loadChapterList(Long courseId);
}
