package com.smartcampus.web.service;

import com.smartcampus.common.dto.DiscussionPostSaveDTO;
import com.smartcampus.common.dto.DiscussionReplySaveDTO;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.vo.DiscussionPostVO;
import com.smartcampus.common.vo.DiscussionReplyVO;
import com.smartcampus.common.vo.PageVO;

import java.util.List;

public interface DiscussionService {
    PageVO<DiscussionPostVO> loadPostList(Long courseId, PageDTO pageDTO);
    DiscussionPostVO createPost(Long userId, DiscussionPostSaveDTO dto);
    List<DiscussionReplyVO> loadReplyList(Long postId);
    DiscussionReplyVO createReply(Long userId, DiscussionReplySaveDTO dto);
}
