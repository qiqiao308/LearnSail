package com.smartcampus.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileQueryDTO extends PageDTO {

    private String fileType;
}
