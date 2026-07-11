package com.smartcampus.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.admin.service.AdminAnnouncementService;
import com.smartcampus.common.dto.AnnouncementSaveDTO;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.mapper.AnnouncementMapper;
import com.smartcampus.common.mapper.CourseMapper;
import com.smartcampus.common.mapper.UserMapper;
import com.smartcampus.common.po.Announcement;
import com.smartcampus.common.po.Course;
import com.smartcampus.common.po.User;
import com.smartcampus.common.vo.AnnouncementVO;
import com.smartcampus.common.vo.PageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminAnnouncementServiceImpl implements AdminAnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CourseMapper courseMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageVO<AnnouncementVO> loadAnnouncementList(PageDTO pageDTO) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Announcement::getCreateTime);

        Page<Announcement> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        Page<Announcement> result = announcementMapper.selectPage(page, wrapper);

        List<AnnouncementVO> voList = result.getRecords().stream().map(announcement -> {
            AnnouncementVO vo = new AnnouncementVO();
            BeanUtils.copyProperties(announcement, vo);
            if (announcement.getCreateTime() != null) {
                vo.setCreateTime(announcement.getCreateTime().format(FORMATTER));
            }
            // resolve publisher name
            if (announcement.getPublisherId() != null) {
                User publisher = userMapper.selectById(announcement.getPublisherId());
                if (publisher != null) {
                    vo.setPublisherName(publisher.getRealName());
                }
            }
            // resolve course title
            if (announcement.getCourseId() != null) {
                Course course = courseMapper.selectById(announcement.getCourseId());
                if (course != null) {
                    vo.setCourseTitle(course.getTitle());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        PageVO<AnnouncementVO> pageVO = new PageVO<>();
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

    @Override
    public void saveAnnouncement(AnnouncementSaveDTO dto, Long publisherId) {
        Announcement announcement = new Announcement();
        BeanUtils.copyProperties(dto, announcement);
        announcement.setPublisherId(publisherId);
        announcement.setCreateTime(LocalDateTime.now());
        announcementMapper.insert(announcement);
    }

    @Override
    public void updateAnnouncement(AnnouncementSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(400, "公告ID不能为空");
        }
        Announcement announcement = announcementMapper.selectById(dto.getId());
        if (announcement == null) {
            throw new BusinessException(404, "公告不存在");
        }
        BeanUtils.copyProperties(dto, announcement);
        announcementMapper.updateById(announcement);
    }

    @Override
    public void deleteAnnouncement(Long announcementId) {
        Announcement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException(404, "公告不存在");
        }
        announcementMapper.deleteById(announcementId);
    }
}
