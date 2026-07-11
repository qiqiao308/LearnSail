package com.smartcampus.common.enums;

import lombok.Getter;

@Getter
public enum ContentTypeEnum {

    VIDEO("VIDEO", "视频"),
    DOCUMENT("DOCUMENT", "文档"),
    RICH_TEXT("RICH_TEXT", "富文本");

    private final String code;
    private final String desc;

    ContentTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
