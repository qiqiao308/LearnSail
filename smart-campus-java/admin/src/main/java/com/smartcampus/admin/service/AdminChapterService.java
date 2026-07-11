package com.smartcampus.admin.service;

import com.smartcampus.common.dto.ChapterSaveDTO;
import com.smartcampus.common.dto.SectionSaveDTO;
import com.smartcampus.common.vo.ChapterVO;

import java.util.List;

public interface AdminChapterService {

    List<ChapterVO> loadChapterList(Long courseId);

    void saveChapter(ChapterSaveDTO dto);

    void updateChapter(ChapterSaveDTO dto);

    void deleteChapter(Long chapterId);

    void saveSection(SectionSaveDTO dto);

    void updateSection(SectionSaveDTO dto);

    void deleteSection(Long sectionId);
}
