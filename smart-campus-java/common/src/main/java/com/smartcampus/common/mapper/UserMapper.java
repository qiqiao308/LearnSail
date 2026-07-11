package com.smartcampus.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.common.po.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
