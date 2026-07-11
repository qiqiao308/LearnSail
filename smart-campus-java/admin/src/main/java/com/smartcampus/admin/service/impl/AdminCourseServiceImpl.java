package com.smartcampus.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.admin.service.AdminCourseService;
import com.smartcampus.common.dto.CourseQueryDTO;
import com.smartcampus.common.dto.CourseSaveDTO;
import com.smartcampus.common.enums.CourseStatusEnum;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.mapper.CourseCategoryMapper;
import com.smartcampus.common.mapper.CourseMapper;
import com.smartcampus.common.mapper.UserMapper;
import com.smartcampus.common.po.Course;
import com.smartcampus.common.po.CourseCategory;
import com.smartcampus.common.po.User;
import com.smartcampus.common.vo.CourseVO;
import com.smartcampus.common.vo.PageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminCourseServiceImpl implements AdminCourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Autowired
    private UserMapper userMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageVO<CourseVO> loadCourseList(CourseQueryDTO queryDTO) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getTitle() != null && !queryDTO.getTitle().isBlank()) {
            wrapper.like(Course::getTitle, queryDTO.getTitle());
        }
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(Course::getCategoryId, queryDTO.getCategoryId());
        }
        if (queryDTO.getTeacherId() != null) {
            wrapper.eq(Course::getTeacherId, queryDTO.getTeacherId());
        }
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isBlank()) {
            wrapper.eq(Course::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(Course::getCreateTime);

        Page<Course> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<Course> result = courseMapper.selectPage(page, wrapper);

        List<CourseVO> voList = result.getRecords().stream().map(course -> {
            CourseVO vo = new CourseVO();
            BeanUtils.copyProperties(course, vo);
            if (course.getCreateTime() != null) {
                vo.setCreateTime(course.getCreateTime().format(FORMATTER));
            }
            // resolve category name
            if (course.getCategoryId() != null) {
                CourseCategory category = courseCategoryMapper.selectById(course.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getName());
                }
            }
            // resolve teacher name
            if (course.getTeacherId() != null) {
                User teacher = userMapper.selectById(course.getTeacherId());
                if (teacher != null) {
                    vo.setTeacherName(teacher.getRealName());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        PageVO<CourseVO> pageVO = new PageVO<>();
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

    @Override
    public void saveCourse(CourseSaveDTO dto) {
        Course course = new Course();
        BeanUtils.copyProperties(dto, course);
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());
        if (course.getStatus() == null) {
            course.setStatus(CourseStatusEnum.DRAFT.getCode());
        }
        courseMapper.insert(course);
    }

    @Override
    public void updateCourse(CourseSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(400, "课程ID不能为空");
        }
        Course course = courseMapper.selectById(dto.getId());
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        BeanUtils.copyProperties(dto, course);
        course.setUpdateTime(LocalDateTime.now());
        courseMapper.updateById(course);
    }

    @Override
    public void deleteCourse(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        courseMapper.deleteById(courseId);
    }

    @Override
    public void publishCourse(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        course.setStatus(CourseStatusEnum.PUBLISHED.getCode());
        course.setUpdateTime(LocalDateTime.now());
        courseMapper.updateById(course);
    }

    @Override
    public void closeCourse(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        course.setStatus(CourseStatusEnum.CLOSED.getCode());
        course.setUpdateTime(LocalDateTime.now());
        courseMapper.updateById(course);
    }
}
