package com.smartcampus.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.common.dto.CourseQueryDTO;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.mapper.CourseCategoryMapper;
import com.smartcampus.common.mapper.CourseChapterMapper;
import com.smartcampus.common.mapper.CourseMapper;
import com.smartcampus.common.mapper.CourseSectionMapper;
import com.smartcampus.common.mapper.EnrollmentMapper;
import com.smartcampus.common.mapper.LearningProgressMapper;
import com.smartcampus.common.mapper.UserFavoriteMapper;
import com.smartcampus.common.mapper.UserMapper;
import com.smartcampus.common.po.Course;
import com.smartcampus.common.po.CourseCategory;
import com.smartcampus.common.po.CourseChapter;
import com.smartcampus.common.po.CourseSection;
import com.smartcampus.common.po.Enrollment;
import com.smartcampus.common.po.LearningProgress;
import com.smartcampus.common.po.User;
import com.smartcampus.common.po.UserFavorite;
import com.smartcampus.common.vo.ChapterVO;
import com.smartcampus.common.vo.CourseDetailVO;
import com.smartcampus.common.vo.CourseVO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.common.vo.SectionVO;
import com.smartcampus.web.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CourseChapterMapper courseChapterMapper;

    @Autowired
    private CourseSectionMapper courseSectionMapper;

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Autowired
    private LearningProgressMapper learningProgressMapper;

    @Override
    public PageVO<CourseVO> loadCourseInfoList(CourseQueryDTO queryDTO) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getTitle() != null && !queryDTO.getTitle().isBlank()) {
            wrapper.like(Course::getTitle, queryDTO.getTitle());
        }
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(Course::getCategoryId, queryDTO.getCategoryId());
        }
        wrapper.eq(Course::getStatus, "PUBLISHED");
        wrapper.orderByDesc(Course::getCreateTime);

        Page<Course> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<Course> result = courseMapper.selectPage(page, wrapper);

        List<CourseVO> voList = result.getRecords().stream().map(course -> {
            CourseVO vo = new CourseVO();
            vo.setId(course.getId());
            vo.setTitle(course.getTitle());
            vo.setDescription(course.getDescription());
            vo.setCoverImage(course.getCoverImage());
            vo.setCategoryId(course.getCategoryId());
            vo.setTeacherId(course.getTeacherId());
            vo.setPrice(course.getPrice());
            vo.setStatus(course.getStatus());
            if (course.getCreateTime() != null) {
                vo.setCreateTime(course.getCreateTime().toString());
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

            // count enrolled students
            LambdaQueryWrapper<Enrollment> enrollmentWrapper = new LambdaQueryWrapper<>();
            enrollmentWrapper.eq(Enrollment::getCourseId, course.getId())
                    .eq(Enrollment::getStatus, "ENROLLED");
            long studentCount = enrollmentMapper.selectCount(enrollmentWrapper);
            vo.setStudentCount((int) studentCount);

            // count chapters
            LambdaQueryWrapper<CourseChapter> chapterWrapper = new LambdaQueryWrapper<>();
            chapterWrapper.eq(CourseChapter::getCourseId, course.getId());
            long chapterCount = courseChapterMapper.selectCount(chapterWrapper);
            vo.setChapterCount((int) chapterCount);

            return vo;
        }).collect(Collectors.toList());

        PageVO<CourseVO> pageVO = new PageVO<>();
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

    @Override
    public CourseDetailVO getCourseInfo(Long courseId, Long userId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(400, "课程不存在");
        }

        CourseDetailVO vo = new CourseDetailVO();
        vo.setId(course.getId());
        vo.setTitle(course.getTitle());
        vo.setDescription(course.getDescription());
        vo.setCoverImage(course.getCoverImage());
        vo.setCategoryId(course.getCategoryId());
        vo.setTeacherId(course.getTeacherId());
        vo.setPrice(course.getPrice());
        vo.setStatus(course.getStatus());
        if (course.getCreateTime() != null) {
            vo.setCreateTime(course.getCreateTime().toString());
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

        // count enrolled students
        LambdaQueryWrapper<Enrollment> enrollmentWrapper = new LambdaQueryWrapper<>();
        enrollmentWrapper.eq(Enrollment::getCourseId, courseId)
                .eq(Enrollment::getStatus, "ENROLLED");
        long studentCount = enrollmentMapper.selectCount(enrollmentWrapper);
        vo.setStudentCount((int) studentCount);

        // count chapters
        LambdaQueryWrapper<CourseChapter> chapterWrapper = new LambdaQueryWrapper<>();
        chapterWrapper.eq(CourseChapter::getCourseId, courseId);
        long chapterCount = courseChapterMapper.selectCount(chapterWrapper);
        vo.setChapterCount((int) chapterCount);

        // load chapters with sections
        List<ChapterVO> chapters = loadChapterList(courseId);
        vo.setChapters(chapters);

        // if user is logged in, check enrollment, favorite, progress
        if (userId != null) {
            // check enrollment
            LambdaQueryWrapper<Enrollment> userEnrollmentWrapper = new LambdaQueryWrapper<>();
            userEnrollmentWrapper.eq(Enrollment::getUserId, userId)
                    .eq(Enrollment::getCourseId, courseId)
                    .eq(Enrollment::getStatus, "ENROLLED");
            long enrollmentCount = enrollmentMapper.selectCount(userEnrollmentWrapper);
            vo.setIsEnrolled(enrollmentCount > 0);

            // check favorite
            LambdaQueryWrapper<UserFavorite> favoriteWrapper = new LambdaQueryWrapper<>();
            favoriteWrapper.eq(UserFavorite::getUserId, userId)
                    .eq(UserFavorite::getCourseId, courseId);
            long favoriteCount = userFavoriteMapper.selectCount(favoriteWrapper);
            vo.setIsFavorite(favoriteCount > 0);

            // calculate progress
            LambdaQueryWrapper<CourseSection> sectionWrapper = new LambdaQueryWrapper<>();
            sectionWrapper.eq(CourseSection::getCourseId, courseId);
            long totalSections = courseSectionMapper.selectCount(sectionWrapper);

            LambdaQueryWrapper<LearningProgress> progressWrapper = new LambdaQueryWrapper<>();
            progressWrapper.eq(LearningProgress::getUserId, userId)
                    .eq(LearningProgress::getCourseId, courseId)
                    .eq(LearningProgress::getIsCompleted, 1);
            long completedSections = learningProgressMapper.selectCount(progressWrapper);

            if (totalSections > 0) {
                vo.setProgress((completedSections * 100.0) / totalSections);
            } else {
                vo.setProgress(0.0);
            }
        }

        return vo;
    }

    @Override
    public List<ChapterVO> loadChapterList(Long courseId) {
        LambdaQueryWrapper<CourseChapter> chapterWrapper = new LambdaQueryWrapper<>();
        chapterWrapper.eq(CourseChapter::getCourseId, courseId)
                .orderByAsc(CourseChapter::getSortOrder);
        List<CourseChapter> chapters = courseChapterMapper.selectList(chapterWrapper);

        List<ChapterVO> chapterVOList = new ArrayList<>();
        for (CourseChapter chapter : chapters) {
            ChapterVO chapterVO = new ChapterVO();
            chapterVO.setId(chapter.getId());
            chapterVO.setCourseId(chapter.getCourseId());
            chapterVO.setTitle(chapter.getTitle());
            chapterVO.setSortOrder(chapter.getSortOrder());

            // query sections for this chapter
            LambdaQueryWrapper<CourseSection> sectionWrapper = new LambdaQueryWrapper<>();
            sectionWrapper.eq(CourseSection::getChapterId, chapter.getId())
                    .orderByAsc(CourseSection::getSortOrder);
            List<CourseSection> sections = courseSectionMapper.selectList(sectionWrapper);

            List<SectionVO> sectionVOList = new ArrayList<>();
            for (CourseSection section : sections) {
                SectionVO sectionVO = new SectionVO();
                sectionVO.setId(section.getId());
                sectionVO.setChapterId(section.getChapterId());
                sectionVO.setCourseId(section.getCourseId());
                sectionVO.setTitle(section.getTitle());
                sectionVO.setContentType(section.getContentType());
                sectionVO.setContentUrl(section.getContentUrl());
                sectionVO.setContentText(section.getContentText());
                sectionVO.setDuration(section.getDuration());
                sectionVO.setSortOrder(section.getSortOrder());
                sectionVO.setIsCompleted(false);
                sectionVOList.add(sectionVO);
            }
            chapterVO.setSections(sectionVOList);
            chapterVOList.add(chapterVO);
        }
        return chapterVOList;
    }
}
