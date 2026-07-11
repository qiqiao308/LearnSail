package com.smartcampus.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageDTO {

    private String username;

    private String realName;

    private String role;

    private Integer status;
}
