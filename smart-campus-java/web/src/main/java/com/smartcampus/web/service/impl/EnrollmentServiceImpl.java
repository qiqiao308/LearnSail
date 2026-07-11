package com.smartcampus.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.mapper.CourseMapper;
import com.smartcampus.common.mapper.CourseSectionMapper;
import com.smartcampus.common.mapper.EnrollmentMapper;
import com.smartcampus.common.mapper.LearningProgressMapper;
import com.smartcampus.common.mapper.UserFavoriteMapper;
import com.smartcampus.common.mapper.UserMapper;
import com.smartcampus.common.po.Course;
import com.smartcampus.common.po.CourseSection;
import com.smartcampus.common.po.Enrollment;
import com.smartcampus.common.po.LearningProgress;
import com.smartcampus.common.po.User;
import com.smartcampus.common.po.UserFavorite;
import com.smartcampus.common.vo.EnrollmentVO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.web.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CourseSectionMapper courseSectionMapper;

    @Autowired
    private LearningProgressMapper learningProgressMapper;

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Override
    public void enrollCourse(Long userId, Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || !"PUBLISHED".equals(course.getStatus())) {
            throw new BusinessException(400, "课程不存在或未发布");
        }

        LambdaQueryWrapper<Enrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enrollment::getUserId, userId)
                .eq(Enrollment::getCourseId, courseId)
                .eq(Enrollment::getStatus, "ENROLLED");
        if (enrollmentMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "已选该课程");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(userId);
        enrollment.setCourseId(courseId);
        enrollment.setStatus("ENROLLED");
        enrollment.setEnrollTime(LocalDateTime.now());
        enrollmentMapper.insert(enrollment);
    }

    @Override
    public void dropCourse(Long userId, Long courseId) {
        LambdaQueryWrapper<Enrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enrollment::getUserId, userId)
                .eq(Enrollment::getCourseId, courseId)
                .eq(Enrollment::getStatus, "ENROLLED");
        Enrollment enrollment = enrollmentMapper.selectOne(wrapper);
        if (enrollment == null) {
            throw new BusinessException(400, "未选该课程");
        }
        enrollment.setStatus("DROPPED");
        enrollmentMapper.updateById(enrollment);
    }

    @Override
    public PageVO<EnrollmentVO> loadMyEnrollments(Long userId, PageDTO pageDTO) {
        LambdaQueryWrapper<Enrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enrollment::getUserId, userId)
                .ne(Enrollment::getStatus, "DROPPED")
                .orderByDesc(Enrollment::getEnrollTime);

        Page<Enrollment> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        Page<Enrollment> result = enrollmentMapper.selectPage(page, wrapper);

        List<EnrollmentVO> voList = result.getRecords().stream().map(enrollment -> {
            EnrollmentVO vo = new EnrollmentVO();
            vo.setId(enrollment.getId());
            vo.setCourseId(enrollment.getCourseId());
            vo.setStatus(enrollment.getStatus());
            if (enrollment.getEnrollTime() != null) {
                vo.setEnrollTime(enrollment.getEnrollTime().toString());
            }

            // get course info
            Course course = courseMapper.selectById(enrollment.getCourseId());
            if (course != null) {
                vo.setCourseTitle(course.getTitle());
                vo.setCourseCover(course.getCoverImage());

                // get teacher name
                if (course.getTeacherId() != null) {
                    User teacher = userMapper.selectById(course.getTeacherId());
                    if (teacher != null) {
                        vo.setTeacherName(teacher.getRealName());
                    }
                }
            }

            // calculate progress
            LambdaQueryWrapper<CourseSection> sectionWrapper = new LambdaQueryWrapper<>();
            sectionWrapper.eq(CourseSection::getCourseId, enrollment.getCourseId());
            long totalSections = courseSectionMapper.selectCount(sectionWrapper);
            vo.setTotalSections((int) totalSections);

            LambdaQueryWrapper<LearningProgress> progressWrapper = new LambdaQueryWrapper<>();
            progressWrapper.eq(LearningProgress::getUserId, userId)
                    .eq(LearningProgress::getCourseId, enrollment.getCourseId())
                    .eq(LearningProgress::getIsCompleted, 1);
            long completedSections = learningProgressMapper.selectCount(progressWrapper);
            vo.setCompletedSections((int) completedSections);

            if (totalSections > 0) {
                vo.setProgress((completedSections * 100.0) / totalSections);
            } else {
                vo.setProgress(0.0);
            }

            return vo;
        }).collect(Collectors.toList());

        PageVO<EnrollmentVO> pageVO = new PageVO<>();
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

    @Override
    public void addFavorite(Long userId, Long courseId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getCourseId, courseId);
        if (userFavoriteMapper.selectCount(wrapper) > 0) {
            return;
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setCourseId(courseId);
        favorite.setCreateTime(LocalDateTime.now());
        userFavoriteMapper.insert(favorite);
    }

    @Override
    public void removeFavorite(Long userId, Long courseId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getCourseId, courseId);
        userFavoriteMapper.delete(wrapper);
    }
}
