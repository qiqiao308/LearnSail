package com.smartcampus.common.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_section")
public class CourseSection {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("chapter_id")
    private Long chapterId;

    @TableField("course_id")
    private Long courseId;

    private String title;

    @TableField("content_type")
    private String contentType;

    @TableField("content_url")
    private String contentUrl;

    @TableField("content_text")
    private String contentText;

    private Integer duration;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("create_time")
    private LocalDateTime createTime;
}
