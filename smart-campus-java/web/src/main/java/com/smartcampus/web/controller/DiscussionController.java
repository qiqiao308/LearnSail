package com.smartcampus.web.controller;

import com.smartcampus.common.dto.DiscussionPostSaveDTO;
import com.smartcampus.common.dto.DiscussionReplySaveDTO;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.DiscussionPostVO;
import com.smartcampus.common.vo.DiscussionReplyVO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.common.vo.ResponseVO;
import com.smartcampus.web.service.DiscussionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/discussion")
public class DiscussionController {

    @Autowired
    private DiscussionService discussionService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/loadPostList")
    public ResponseVO<PageVO<DiscussionPostVO>> loadPostList(@RequestParam Long courseId,
                                                              @RequestBody PageDTO pageDTO) {
        return ResponseVO.success(discussionService.loadPostList(courseId, pageDTO));
    }

    @PostMapping("/createPost")
    public ResponseVO<DiscussionPostVO> createPost(@RequestBody @Valid DiscussionPostSaveDTO dto,
                                                    HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return ResponseVO.success(discussionService.createPost(userId, dto));
    }

    @GetMapping("/loadReplyList")
    public ResponseVO<List<DiscussionReplyVO>> loadReplyList(@RequestParam Long postId) {
        return ResponseVO.success(discussionService.loadReplyList(postId));
    }

    @PostMapping("/createReply")
    public ResponseVO<DiscussionReplyVO> createReply(@RequestBody @Valid DiscussionReplySaveDTO dto,
                                                      HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return ResponseVO.success(discussionService.createReply(userId, dto));
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserId(token);
        }
        return null;
    }
}
