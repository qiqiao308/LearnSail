package com.smartcampus.web.service;

public interface ProgressService {
    void markSectionComplete(Long userId, Long sectionId, Long courseId);
    Double getCourseProgress(Long userId, Long courseId);
}
