package com.smartcampus.common.vo;

import lombok.Data;

@Data
public class UserVO {

    private Long id;

    private String username;

    private String realName;

    private String email;

    private String phone;

    private String role;

    private String avatar;

    private Integer status;

    private String createTime;
}
