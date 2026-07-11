package com.smartcampus.common.enums;

import lombok.Getter;

@Getter
public enum ExamStatusEnum {

    NOT_STARTED("NOT_STARTED", "未开始"),
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    ENDED("ENDED", "已结束");

    private final String code;
    private final String desc;

    ExamStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
