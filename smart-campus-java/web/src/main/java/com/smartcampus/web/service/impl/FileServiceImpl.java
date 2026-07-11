package com.smartcampus.web.service.impl;

import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.mapper.FileInfoMapper;
import com.smartcampus.common.po.FileInfo;
import com.smartcampus.common.service.FileStorageService;
import com.smartcampus.common.vo.FileInfoVO;
import com.smartcampus.web.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private FileStorageService fileStorageService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public FileInfoVO uploadFile(MultipartFile file, Long userId) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        String filePath = fileStorageService.store(file);

        FileInfo fileInfo = new FileInfo();
        fileInfo.setOriginalName(file.getOriginalFilename());
        fileInfo.setStoredName(getStoredName(filePath));
        fileInfo.setFilePath(filePath);
        fileInfo.setFileSize(file.getSize());
        fileInfo.setFileType(getFileExtension(file.getOriginalFilename()));
        fileInfo.setMimeType(file.getContentType());
        fileInfo.setUploaderId(userId);
        fileInfo.setCreateTime(LocalDateTime.now());
        fileInfoMapper.insert(fileInfo);

        FileInfoVO vo = new FileInfoVO();
        vo.setId(fileInfo.getId());
        vo.setOriginalName(fileInfo.getOriginalName());
        vo.setStoredName(fileInfo.getStoredName());
        vo.setFilePath(fileInfo.getFilePath());
        vo.setFileSize(fileInfo.getFileSize());
        vo.setFileType(fileInfo.getFileType());
        vo.setMimeType(fileInfo.getMimeType());
        vo.setUploaderId(fileInfo.getUploaderId());
        if (fileInfo.getCreateTime() != null) {
            vo.setCreateTime(fileInfo.getCreateTime().format(FORMATTER));
        }
        vo.setUrl("/api/file/downloadFile/" + fileInfo.getId());

        return vo;
    }

    @Override
    public FileInfo getFileInfo(Long fileId) {
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);
        if (fileInfo == null) {
            throw new BusinessException(400, "文件不存在");
        }
        return fileInfo;
    }

    private String getStoredName(String filePath) {
        if (filePath == null || !filePath.contains("/")) {
            return filePath;
        }
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
