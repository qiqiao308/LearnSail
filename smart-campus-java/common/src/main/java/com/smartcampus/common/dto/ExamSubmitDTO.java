package com.smartcampus.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExamSubmitDTO {

    private Long examRecordId;

    private List<AnswerDTO> answers;
}
