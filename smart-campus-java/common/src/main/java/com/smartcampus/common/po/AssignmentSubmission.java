package com.smartcampus.common.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("assignment_submission")
public class AssignmentSubmission {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("assignment_id")
    private Long assignmentId;

    @TableField("user_id")
    private Long userId;

    private String content;

    @TableField("file_url")
    private String fileUrl;

    private Integer score;

    private String comment;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("grade_time")
    private LocalDateTime gradeTime;
}
