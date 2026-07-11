package com.smartcampus.common.vo;

import lombok.Data;

@Data
public class DiscussionReplyVO {

    private Long id;

    private Long postId;

    private Long userId;

    private String username;

    private String userAvatar;

    private Long parentReplyId;

    private String parentUsername;

    private String content;

    private String createTime;
}
