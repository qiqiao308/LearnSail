package com.smartcampus.common.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileStorageService {

    /**
     * 存储文件
     * @param file 上传的文件
     * @return 文件存储路径(相对路径)
     */
    String store(MultipartFile file);

    /**
     * 获取文件流
     * @param filePath 文件存储路径
     * @return 文件输入流
     */
    InputStream getFileStream(String filePath);

    /**
     * 删除文件
     * @param filePath 文件存储路径
     * @return 是否删除成功
     */
    boolean delete(String filePath);

    /**
     * 获取文件大小
     * @param filePath 文件存储路径
     * @return 文件大小(字节)
     */
    long getFileSize(String filePath);
}
