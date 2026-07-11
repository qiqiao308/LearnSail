package com.smartcampus.common.vo;

import lombok.Data;

@Data
public class DiscussionPostVO {

    private Long id;

    private Long courseId;

    private Long userId;

    private String username;

    private String userAvatar;

    private String title;

    private String content;

    private Integer replyCount;

    private String createTime;
}
