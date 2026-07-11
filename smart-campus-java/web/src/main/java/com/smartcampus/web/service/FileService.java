package com.smartcampus.web.service;

import com.smartcampus.common.po.FileInfo;
import com.smartcampus.common.vo.FileInfoVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileInfoVO uploadFile(MultipartFile file, Long userId);
    FileInfo getFileInfo(Long fileId);
}
