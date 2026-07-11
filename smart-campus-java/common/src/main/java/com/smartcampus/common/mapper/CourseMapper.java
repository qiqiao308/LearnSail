package com.smartcampus.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.common.po.Course;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
