package com.smartcampus.admin.service;

import com.smartcampus.common.dto.FileQueryDTO;
import com.smartcampus.common.vo.FileInfoVO;
import com.smartcampus.common.vo.PageVO;
import org.springframework.web.multipart.MultipartFile;

public interface AdminFileService {

    FileInfoVO uploadFile(MultipartFile file, Long userId);

    PageVO<FileInfoVO> loadFileList(FileQueryDTO queryDTO);

    void deleteFile(Long fileId);
}
