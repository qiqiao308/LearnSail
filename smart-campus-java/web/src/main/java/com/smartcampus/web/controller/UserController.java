package com.smartcampus.web.controller;

import com.smartcampus.common.dto.ChangePasswordDTO;
import com.smartcampus.common.dto.LoginDTO;
import com.smartcampus.common.dto.RegisterDTO;
import com.smartcampus.common.dto.UpdateProfileDTO;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.LoginVO;
import com.smartcampus.common.vo.ResponseVO;
import com.smartcampus.common.vo.UserVO;
import com.smartcampus.web.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseVO<LoginVO> login(@RequestBody @Valid LoginDTO loginDTO) {
        return ResponseVO.success(userService.login(loginDTO));
    }

    @PostMapping("/register")
    public ResponseVO<LoginVO> register(@RequestBody @Valid RegisterDTO registerDTO) {
        return ResponseVO.success(userService.register(registerDTO));
    }

    @GetMapping("/getCurrentUser")
    public ResponseVO<UserVO> getCurrentUser(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return ResponseVO.success(userService.getCurrentUser(userId));
    }

    @PutMapping("/updateProfile")
    public ResponseVO<Void> updateProfile(@RequestBody UpdateProfileDTO dto, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        userService.updateProfile(userId, dto);
        return ResponseVO.success("修改成功", null);
    }

    @PutMapping("/changePassword")
    public ResponseVO<Void> changePassword(@RequestBody @Valid ChangePasswordDTO dto, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        userService.changePassword(userId, dto);
        return ResponseVO.success("密码修改成功", null);
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
