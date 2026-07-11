package com.smartcampus.common.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("exam_question_record")
public class ExamQuestionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("exam_record_id")
    private Long examRecordId;

    @TableField("question_id")
    private Long questionId;

    @TableField("user_answer")
    private String userAnswer;

    @TableField("is_correct")
    private Integer isCorrect;

    private Integer score;

    @TableField("question_type")
    private String questionType;
}
