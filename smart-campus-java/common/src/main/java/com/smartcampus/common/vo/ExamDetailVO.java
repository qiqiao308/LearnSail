package com.smartcampus.common.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExamDetailVO extends ExamVO {

    private List<QuestionVO> questions;

    private Long examRecordId;
}
