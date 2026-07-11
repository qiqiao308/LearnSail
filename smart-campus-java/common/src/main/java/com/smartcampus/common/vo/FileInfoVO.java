package com.smartcampus.common.vo;

import lombok.Data;

@Data
public class FileInfoVO {

    private Long id;

    private String originalName;

    private String storedName;

    private String filePath;

    private Long fileSize;

    private String fileType;

    private String mimeType;

    private Long uploaderId;

    private String createTime;

    private String url;
}
