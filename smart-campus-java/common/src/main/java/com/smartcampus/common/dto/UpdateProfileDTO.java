package com.smartcampus.common.dto;

import lombok.Data;

@Data
public class UpdateProfileDTO {

    private String realName;

    private String email;

    private String phone;

    private String avatar;
}
