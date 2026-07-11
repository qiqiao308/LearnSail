package com.smartcampus.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcampus.common.mapper.CourseSectionMapper;
import com.smartcampus.common.mapper.LearningProgressMapper;
import com.smartcampus.common.po.CourseSection;
import com.smartcampus.common.po.LearningProgress;
import com.smartcampus.web.service.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProgressServiceImpl implements ProgressService {

    @Autowired
    private LearningProgressMapper learningProgressMapper;

    @Autowired
    private CourseSectionMapper courseSectionMapper;

    @Override
    public void markSectionComplete(Long userId, Long sectionId, Long courseId) {
        LambdaQueryWrapper<LearningProgress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningProgress::getUserId, userId)
                .eq(LearningProgress::getSectionId, sectionId)
                .eq(LearningProgress::getCourseId, courseId);
        LearningProgress progress = learningProgressMapper.selectOne(wrapper);
        if (progress == null) {
            progress = new LearningProgress();
            progress.setUserId(userId);
            progress.setSectionId(sectionId);
            progress.setCourseId(courseId);
            progress.setIsCompleted(1);
            progress.setCompletedTime(LocalDateTime.now());
            learningProgressMapper.insert(progress);
        } else {
            progress.setIsCompleted(1);
            progress.setCompletedTime(LocalDateTime.now());
            learningProgressMapper.updateById(progress);
        }
    }

    @Override
    public Double getCourseProgress(Long userId, Long courseId) {
        LambdaQueryWrapper<CourseSection> sectionWrapper = new LambdaQueryWrapper<>();
        sectionWrapper.eq(CourseSection::getCourseId, courseId);
        long totalSections = courseSectionMapper.selectCount(sectionWrapper);

        LambdaQueryWrapper<LearningProgress> progressWrapper = new LambdaQueryWrapper<>();
        progressWrapper.eq(LearningProgress::getUserId, userId)
                .eq(LearningProgress::getCourseId, courseId)
                .eq(LearningProgress::getIsCompleted, 1);
        long completedSections = learningProgressMapper.selectCount(progressWrapper);

        if (totalSections == 0) {
            return 0.0;
        }
        return (completedSections * 100.0) / totalSections;
    }
}
