package com.smartcampus.web.service;

import com.smartcampus.common.dto.ChangePasswordDTO;
import com.smartcampus.common.dto.LoginDTO;
import com.smartcampus.common.dto.RegisterDTO;
import com.smartcampus.common.dto.UpdateProfileDTO;
import com.smartcampus.common.vo.LoginVO;
import com.smartcampus.common.vo.UserVO;

public interface UserService {
    LoginVO login(LoginDTO loginDTO);
    LoginVO register(RegisterDTO registerDTO);
    UserVO getCurrentUser(Long userId);
    void updateProfile(Long userId, UpdateProfileDTO dto);
    void changePassword(Long userId, ChangePasswordDTO dto);
}
