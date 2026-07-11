package com.smartcampus.common.enums;

import lombok.Getter;

@Getter
public enum QuestionTypeEnum {

    SINGLE_CHOICE("SINGLE_CHOICE", "单选题"),
    MULTIPLE_CHOICE("MULTIPLE_CHOICE", "多选题"),
    TRUE_FALSE("TRUE_FALSE", "判断题"),
    SHORT_ANSWER("SHORT_ANSWER", "简答题");

    private final String code;
    private final String desc;

    QuestionTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
