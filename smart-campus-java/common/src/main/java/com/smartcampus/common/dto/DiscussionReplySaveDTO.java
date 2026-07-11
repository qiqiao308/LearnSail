package com.smartcampus.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DiscussionReplySaveDTO {

    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    private Long parentReplyId;

    @NotBlank(message = "回复内容不能为空")
    private String content;
}
