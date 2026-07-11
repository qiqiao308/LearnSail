package com.smartcampus.admin.controller;

import com.smartcampus.admin.service.AdminUserService;
import com.smartcampus.common.dto.LoginDTO;
import com.smartcampus.common.dto.UserQueryDTO;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.mapper.UserMapper;
import com.smartcampus.common.po.User;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.utils.Md5Util;
import com.smartcampus.common.vo.LoginVO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.common.vo.ResponseVO;
import com.smartcampus.common.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/adminUser")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private Md5Util md5Util;

    @PostMapping("/login")
    public ResponseVO<LoginVO> login(@RequestBody @Valid LoginDTO loginDTO) {
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, loginDTO.getUsername()));
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!md5Util.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }
        if (!"ADMIN".equals(user.getRole()) && !"TEACHER".equals(user.getRole())) {
            throw new BusinessException(403, "无权限登录管理后台");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setRole(user.getRole());
        loginVO.setAvatar(user.getAvatar());

        return ResponseVO.success(loginVO);
    }

    @PostMapping("/loadUserList")
    public ResponseVO<PageVO<UserVO>> loadUserList(@RequestBody UserQueryDTO queryDTO) {
        PageVO<UserVO> result = adminUserService.loadUserList(queryDTO);
        return ResponseVO.success(result);
    }

    @PutMapping("/updateUserStatus")
    public ResponseVO<Void> updateUserStatus(@RequestParam Long userId, @RequestParam Integer status) {
        adminUserService.updateUserStatus(userId, status);
        return ResponseVO.success(null);
    }

    @DeleteMapping("/deleteUser")
    public ResponseVO<Void> deleteUser(@RequestParam Long userId) {
        adminUserService.deleteUser(userId);
        return ResponseVO.success(null);
    }

    @PutMapping("/resetPassword")
    public ResponseVO<Void> resetPassword(@RequestParam Long userId) {
        adminUserService.resetPassword(userId);
        return ResponseVO.success(null);
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserId(token);
        }
        return null;
    }
}
