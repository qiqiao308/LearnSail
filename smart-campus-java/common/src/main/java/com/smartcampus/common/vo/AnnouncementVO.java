package com.smartcampus.common.vo;

import lombok.Data;

@Data
public class AnnouncementVO {

    private Long id;

    private String title;

    private String content;

    private String type;

    private Long courseId;

    private String courseTitle;

    private String publisherName;

    private String createTime;

    private Boolean isRead;
}
