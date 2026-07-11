package com.smartcampus.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.admin.service.AdminLogService;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.mapper.SystemLogMapper;
import com.smartcampus.common.po.SystemLog;
import com.smartcampus.common.vo.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminLogServiceImpl implements AdminLogService {

    @Autowired
    private SystemLogMapper systemLogMapper;

    @Override
    public PageVO<SystemLog> loadLogList(PageDTO pageDTO) {
        LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SystemLog::getCreateTime);

        Page<SystemLog> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        Page<SystemLog> result = systemLogMapper.selectPage(page, wrapper);

        PageVO<SystemLog> pageVO = new PageVO<>();
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(result.getRecords());
        return pageVO;
    }
}
