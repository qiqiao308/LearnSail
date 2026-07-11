package com.smartcampus.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcampus.admin.service.AdminChapterService;
import com.smartcampus.common.dto.ChapterSaveDTO;
import com.smartcampus.common.dto.SectionSaveDTO;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.mapper.CourseChapterMapper;
import com.smartcampus.common.mapper.CourseSectionMapper;
import com.smartcampus.common.po.CourseChapter;
import com.smartcampus.common.po.CourseSection;
import com.smartcampus.common.vo.ChapterVO;
import com.smartcampus.common.vo.SectionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminChapterServiceImpl implements AdminChapterService {

    @Autowired
    private CourseChapterMapper courseChapterMapper;

    @Autowired
    private CourseSectionMapper courseSectionMapper;

    @Override
    public List<ChapterVO> loadChapterList(Long courseId) {
        LambdaQueryWrapper<CourseChapter> chapterWrapper = new LambdaQueryWrapper<>();
        chapterWrapper.eq(CourseChapter::getCourseId, courseId);
        chapterWrapper.orderByAsc(CourseChapter::getSortOrder);
        List<CourseChapter> chapters = courseChapterMapper.selectList(chapterWrapper);

        List<ChapterVO> result = new ArrayList<>();
        for (CourseChapter chapter : chapters) {
            ChapterVO chapterVO = new ChapterVO();
            BeanUtils.copyProperties(chapter, chapterVO);

            LambdaQueryWrapper<CourseSection> sectionWrapper = new LambdaQueryWrapper<>();
            sectionWrapper.eq(CourseSection::getChapterId, chapter.getId());
            sectionWrapper.orderByAsc(CourseSection::getSortOrder);
            List<CourseSection> sections = courseSectionMapper.selectList(sectionWrapper);

            List<SectionVO> sectionVOs = new ArrayList<>();
            for (CourseSection section : sections) {
                SectionVO sectionVO = new SectionVO();
                BeanUtils.copyProperties(section, sectionVO);
                sectionVOs.add(sectionVO);
            }
            chapterVO.setSections(sectionVOs);
            result.add(chapterVO);
        }
        return result;
    }

    @Override
    public void saveChapter(ChapterSaveDTO dto) {
        CourseChapter chapter = new CourseChapter();
        BeanUtils.copyProperties(dto, chapter);
        chapter.setCreateTime(LocalDateTime.now());
        courseChapterMapper.insert(chapter);
    }

    @Override
    public void updateChapter(ChapterSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(400, "章节ID不能为空");
        }
        CourseChapter chapter = courseChapterMapper.selectById(dto.getId());
        if (chapter == null) {
            throw new BusinessException(404, "章节不存在");
        }
        BeanUtils.copyProperties(dto, chapter);
        courseChapterMapper.updateById(chapter);
    }

    @Override
    @Transactional
    public void deleteChapter(Long chapterId) {
        CourseChapter chapter = courseChapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BusinessException(404, "章节不存在");
        }
        // delete all sections under this chapter
        LambdaQueryWrapper<CourseSection> sectionWrapper = new LambdaQueryWrapper<>();
        sectionWrapper.eq(CourseSection::getChapterId, chapterId);
        courseSectionMapper.delete(sectionWrapper);
        // delete chapter itself
        courseChapterMapper.deleteById(chapterId);
    }

    @Override
    public void saveSection(SectionSaveDTO dto) {
        CourseSection section = new CourseSection();
        BeanUtils.copyProperties(dto, section);
        section.setCreateTime(LocalDateTime.now());
        courseSectionMapper.insert(section);
    }

    @Override
    public void updateSection(SectionSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(400, "小节ID不能为空");
        }
        CourseSection section = courseSectionMapper.selectById(dto.getId());
        if (section == null) {
            throw new BusinessException(404, "小节不存在");
        }
        BeanUtils.copyProperties(dto, section);
        courseSectionMapper.updateById(section);
    }

    @Override
    public void deleteSection(Long sectionId) {
        CourseSection section = courseSectionMapper.selectById(sectionId);
        if (section == null) {
            throw new BusinessException(404, "小节不存在");
        }
        courseSectionMapper.deleteById(sectionId);
    }
}
