package com.smartcampus.web.controller;

import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.AnnouncementVO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.common.vo.ResponseVO;
import com.smartcampus.web.service.AnnouncementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/loadAnnouncementList")
    public ResponseVO<PageVO<AnnouncementVO>> loadAnnouncementList(@RequestBody PageDTO pageDTO,
                                                                    HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return ResponseVO.success(announcementService.loadAnnouncementList(userId, pageDTO));
    }

    @PostMapping("/markRead")
    public ResponseVO<Void> markRead(@RequestParam Long announcementId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        announcementService.markRead(userId, announcementId);
        return ResponseVO.success("标记已读成功", null);
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
