package com.smartcampus.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.mapper.AnnouncementMapper;
import com.smartcampus.common.mapper.AnnouncementReadMapper;
import com.smartcampus.common.mapper.CourseMapper;
import com.smartcampus.common.mapper.EnrollmentMapper;
import com.smartcampus.common.mapper.UserMapper;
import com.smartcampus.common.po.Announcement;
import com.smartcampus.common.po.AnnouncementRead;
import com.smartcampus.common.po.Course;
import com.smartcampus.common.po.Enrollment;
import com.smartcampus.common.po.User;
import com.smartcampus.common.vo.AnnouncementVO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.web.service.AnnouncementService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Autowired
    private AnnouncementReadMapper announcementReadMapper;

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CourseMapper courseMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageVO<AnnouncementVO> loadAnnouncementList(Long userId, PageDTO pageDTO) {
        // get enrolled course IDs
        LambdaQueryWrapper<Enrollment> enrollmentWrapper = new LambdaQueryWrapper<>();
        enrollmentWrapper.eq(Enrollment::getUserId, userId);
        enrollmentWrapper.ne(Enrollment::getStatus, "DROPPED");
        List<Enrollment> enrollments = enrollmentMapper.selectList(enrollmentWrapper);
        List<Long> enrolledCourseIds = enrollments.stream()
                .map(Enrollment::getCourseId)
                .collect(Collectors.toList());

        // build query: type=SYSTEM OR courseId in enrolledCourseIds
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        if (!enrolledCourseIds.isEmpty()) {
            wrapper.and(w -> w.eq(Announcement::getType, "SYSTEM")
                    .or().in(Announcement::getCourseId, enrolledCourseIds));
        } else {
            wrapper.eq(Announcement::getType, "SYSTEM");
        }
        wrapper.orderByDesc(Announcement::getCreateTime);

        Page<Announcement> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        Page<Announcement> result = announcementMapper.selectPage(page, wrapper);

        List<AnnouncementVO> voList = result.getRecords().stream().map(announcement -> {
            AnnouncementVO vo = new AnnouncementVO();
            BeanUtils.copyProperties(announcement, vo);
            if (announcement.getCreateTime() != null) {
                vo.setCreateTime(announcement.getCreateTime().format(FORMATTER));
            }

            // course title
            if (announcement.getCourseId() != null) {
                Course course = courseMapper.selectById(announcement.getCourseId());
                if (course != null) {
                    vo.setCourseTitle(course.getTitle());
                }
            }

            // publisher name
            if (announcement.getPublisherId() != null) {
                User publisher = userMapper.selectById(announcement.getPublisherId());
                if (publisher != null) {
                    vo.setPublisherName(publisher.getRealName());
                }
            }

            // check if read
            LambdaQueryWrapper<AnnouncementRead> readWrapper = new LambdaQueryWrapper<>();
            readWrapper.eq(AnnouncementRead::getAnnouncementId, announcement.getId());
            readWrapper.eq(AnnouncementRead::getUserId, userId);
            vo.setIsRead(announcementReadMapper.selectCount(readWrapper) > 0);

            return vo;
        }).collect(Collectors.toList());

        PageVO<AnnouncementVO> pageVO = new PageVO<>();
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

    @Override
    public void markRead(Long userId, Long announcementId) {
        LambdaQueryWrapper<AnnouncementRead> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnnouncementRead::getAnnouncementId, announcementId);
        wrapper.eq(AnnouncementRead::getUserId, userId);
        if (announcementReadMapper.selectCount(wrapper) == 0) {
            AnnouncementRead read = new AnnouncementRead();
            read.setAnnouncementId(announcementId);
            read.setUserId(userId);
            read.setReadTime(LocalDateTime.now());
            announcementReadMapper.insert(read);
        }
    }
}
