package com.smartcampus.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.common.dto.DiscussionPostSaveDTO;
import com.smartcampus.common.dto.DiscussionReplySaveDTO;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.mapper.DiscussionPostMapper;
import com.smartcampus.common.mapper.DiscussionReplyMapper;
import com.smartcampus.common.mapper.UserMapper;
import com.smartcampus.common.po.DiscussionPost;
import com.smartcampus.common.po.DiscussionReply;
import com.smartcampus.common.po.User;
import com.smartcampus.common.vo.DiscussionPostVO;
import com.smartcampus.common.vo.DiscussionReplyVO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.web.service.DiscussionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscussionServiceImpl implements DiscussionService {

    @Autowired
    private DiscussionPostMapper discussionPostMapper;

    @Autowired
    private DiscussionReplyMapper discussionReplyMapper;

    @Autowired
    private UserMapper userMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageVO<DiscussionPostVO> loadPostList(Long courseId, PageDTO pageDTO) {
        LambdaQueryWrapper<DiscussionPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiscussionPost::getCourseId, courseId);
        wrapper.orderByDesc(DiscussionPost::getCreateTime);

        Page<DiscussionPost> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        Page<DiscussionPost> result = discussionPostMapper.selectPage(page, wrapper);

        List<DiscussionPostVO> voList = result.getRecords().stream().map(post -> {
            DiscussionPostVO vo = new DiscussionPostVO();
            BeanUtils.copyProperties(post, vo);
            if (post.getCreateTime() != null) {
                vo.setCreateTime(post.getCreateTime().format(FORMATTER));
            }

            // user info
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setUserAvatar(user.getAvatar());
            }

            // reply count
            LambdaQueryWrapper<DiscussionReply> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(DiscussionReply::getPostId, post.getId());
            vo.setReplyCount(discussionReplyMapper.selectCount(replyWrapper).intValue());

            return vo;
        }).collect(Collectors.toList());

        PageVO<DiscussionPostVO> pageVO = new PageVO<>();
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

    @Override
    public DiscussionPostVO createPost(Long userId, DiscussionPostSaveDTO dto) {
        DiscussionPost post = new DiscussionPost();
        post.setCourseId(dto.getCourseId());
        post.setUserId(userId);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        discussionPostMapper.insert(post);

        DiscussionPostVO vo = new DiscussionPostVO();
        BeanUtils.copyProperties(post, vo);
        if (post.getCreateTime() != null) {
            vo.setCreateTime(post.getCreateTime().format(FORMATTER));
        }

        User user = userMapper.selectById(userId);
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setUserAvatar(user.getAvatar());
        }
        vo.setReplyCount(0);

        return vo;
    }

    @Override
    public List<DiscussionReplyVO> loadReplyList(Long postId) {
        LambdaQueryWrapper<DiscussionReply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiscussionReply::getPostId, postId);
        wrapper.orderByAsc(DiscussionReply::getCreateTime);
        List<DiscussionReply> replies = discussionReplyMapper.selectList(wrapper);

        // build VO list
        List<DiscussionReplyVO> allVOs = new ArrayList<>();
        for (DiscussionReply reply : replies) {
            DiscussionReplyVO vo = new DiscussionReplyVO();
            BeanUtils.copyProperties(reply, vo);
            if (reply.getCreateTime() != null) {
                vo.setCreateTime(reply.getCreateTime().format(FORMATTER));
            }

            User user = userMapper.selectById(reply.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setUserAvatar(user.getAvatar());
            }

            if (reply.getParentReplyId() != null) {
                DiscussionReply parentReply = discussionReplyMapper.selectById(reply.getParentReplyId());
                if (parentReply != null) {
                    User parentUser = userMapper.selectById(parentReply.getUserId());
                    if (parentUser != null) {
                        vo.setParentUsername(parentUser.getUsername());
                    }
                }
            }

            allVOs.add(vo);
        }

        // organize: top-level first, then children
        List<DiscussionReplyVO> result = new ArrayList<>();
        for (DiscussionReplyVO vo : allVOs) {
            if (vo.getParentReplyId() == null) {
                result.add(vo);
            }
        }
        for (DiscussionReplyVO vo : allVOs) {
            if (vo.getParentReplyId() != null) {
                result.add(vo);
            }
        }

        return result;
    }

    @Override
    public DiscussionReplyVO createReply(Long userId, DiscussionReplySaveDTO dto) {
        DiscussionReply reply = new DiscussionReply();
        reply.setPostId(dto.getPostId());
        reply.setUserId(userId);
        reply.setParentReplyId(dto.getParentReplyId());
        reply.setContent(dto.getContent());
        reply.setCreateTime(LocalDateTime.now());
        discussionReplyMapper.insert(reply);

        DiscussionReplyVO vo = new DiscussionReplyVO();
        BeanUtils.copyProperties(reply, vo);
        if (reply.getCreateTime() != null) {
            vo.setCreateTime(reply.getCreateTime().format(FORMATTER));
        }

        User user = userMapper.selectById(userId);
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setUserAvatar(user.getAvatar());
        }

        if (reply.getParentReplyId() != null) {
            DiscussionReply parentReply = discussionReplyMapper.selectById(reply.getParentReplyId());
            if (parentReply != null) {
                User parentUser = userMapper.selectById(parentReply.getUserId());
                if (parentUser != null) {
                    vo.setParentUsername(parentUser.getUsername());
                }
            }
        }

        return vo;
    }
}