package com.smartcampus.admin.service;

import com.smartcampus.common.dto.AnnouncementSaveDTO;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.vo.AnnouncementVO;
import com.smartcampus.common.vo.PageVO;

public interface AdminAnnouncementService {

    PageVO<AnnouncementVO> loadAnnouncementList(PageDTO pageDTO);

    void saveAnnouncement(AnnouncementSaveDTO dto, Long publisherId);

    void updateAnnouncement(AnnouncementSaveDTO dto);

    void deleteAnnouncement(Long announcementId);
}
