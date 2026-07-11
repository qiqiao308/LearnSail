package com.smartcampus.web.service;

import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.vo.AnnouncementVO;
import com.smartcampus.common.vo.PageVO;

public interface AnnouncementService {
    PageVO<AnnouncementVO> loadAnnouncementList(Long userId, PageDTO pageDTO);
    void markRead(Long userId, Long announcementId);
}
