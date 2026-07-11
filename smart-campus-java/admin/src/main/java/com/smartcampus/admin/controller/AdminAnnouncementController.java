package com.smartcampus.admin.controller;

import com.smartcampus.admin.service.AdminAnnouncementService;
import com.smartcampus.common.dto.AnnouncementSaveDTO;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.AnnouncementVO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.common.vo.ResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/adminAnnouncement")
public class AdminAnnouncementController {

    @Autowired
    private AdminAnnouncementService adminAnnouncementService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/loadAnnouncementList")
    public ResponseVO<PageVO<AnnouncementVO>> loadAnnouncementList(@RequestBody PageDTO pageDTO) {
        PageVO<AnnouncementVO> result = adminAnnouncementService.loadAnnouncementList(pageDTO);
        return ResponseVO.success(result);
    }

    @PostMapping("/saveAnnouncement")
    public ResponseVO<Void> saveAnnouncement(@RequestBody @Valid AnnouncementSaveDTO dto,
                                              HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        adminAnnouncementService.saveAnnouncement(dto, userId);
        return ResponseVO.success(null);
    }

    @PutMapping("/updateAnnouncement")
    public ResponseVO<Void> updateAnnouncement(@RequestBody AnnouncementSaveDTO dto) {
        adminAnnouncementService.updateAnnouncement(dto);
        return ResponseVO.success(null);
    }

    @DeleteMapping("/deleteAnnouncement")
    public ResponseVO<Void> deleteAnnouncement(@RequestParam Long announcementId) {
        adminAnnouncementService.deleteAnnouncement(announcementId);
        return ResponseVO.success(null);
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
