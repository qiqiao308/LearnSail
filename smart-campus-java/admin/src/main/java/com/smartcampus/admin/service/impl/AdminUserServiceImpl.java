package com.smartcampus.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.admin.service.AdminUserService;
import com.smartcampus.common.dto.UserQueryDTO;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.mapper.UserMapper;
import com.smartcampus.common.po.User;
import com.smartcampus.common.utils.Md5Util;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.common.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private Md5Util md5Util;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageVO<UserVO> loadUserList(UserQueryDTO queryDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getUsername() != null && !queryDTO.getUsername().isBlank()) {
            wrapper.like(User::getUsername, queryDTO.getUsername());
        }
        if (queryDTO.getRealName() != null && !queryDTO.getRealName().isBlank()) {
            wrapper.like(User::getRealName, queryDTO.getRealName());
        }
        if (queryDTO.getRole() != null && !queryDTO.getRole().isBlank()) {
            wrapper.eq(User::getRole, queryDTO.getRole());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(User::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<User> result = userMapper.selectPage(page, wrapper);

        List<UserVO> voList = result.getRecords().stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            if (user.getCreateTime() != null) {
                vo.setCreateTime(user.getCreateTime().format(FORMATTER));
            }
            return vo;
        }).collect(Collectors.toList());

        PageVO<UserVO> pageVO = new PageVO<>();
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        userMapper.deleteById(userId);
    }

    @Override
    public void resetPassword(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setPassword(md5Util.encode("123456"));
        userMapper.updateById(user);
    }
}
