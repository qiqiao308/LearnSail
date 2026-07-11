package com.smartcampus.common.service;

import com.smartcampus.common.utils.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload.path:./upload}")
    private String uploadBasePath;

    @Override
    public String store(MultipartFile file) {
        try {
            String datePath = FileUtil.getDatePath();
            String storedName = FileUtil.generateStoredName(file.getOriginalFilename());
            String relativePath = datePath + "/" + storedName;

            Path targetPath = Paths.get(uploadBasePath, relativePath);
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath.toFile());

            return relativePath;
        } catch (IOException e) {
            throw new RuntimeException("文件存储失败: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream getFileStream(String filePath) {
        try {
            Path targetPath = Paths.get(uploadBasePath, filePath);
            return new FileInputStream(targetPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String filePath) {
        try {
            Path targetPath = Paths.get(uploadBasePath, filePath);
            return Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public long getFileSize(String filePath) {
        try {
            Path targetPath = Paths.get(uploadBasePath, filePath);
            return Files.size(targetPath);
        } catch (IOException e) {
            return 0;
        }
    }
}
