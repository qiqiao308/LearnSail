package com.smartcampus.admin.service;

import com.smartcampus.common.dto.UserQueryDTO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.common.vo.UserVO;

public interface AdminUserService {

    PageVO<UserVO> loadUserList(UserQueryDTO queryDTO);

    void updateUserStatus(Long userId, Integer status);

    void deleteUser(Long userId);

    void resetPassword(Long userId);
}
