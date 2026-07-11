package com.smartcampus.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.common.po.Announcement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
