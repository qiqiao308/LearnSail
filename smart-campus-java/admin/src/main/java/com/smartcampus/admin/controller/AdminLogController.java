package com.smartcampus.admin.controller;

import com.smartcampus.admin.service.AdminLogService;
import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.po.SystemLog;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.common.vo.ResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/adminLog")
public class AdminLogController {

    @Autowired
    private AdminLogService adminLogService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/loadLogList")
    public ResponseVO<PageVO<SystemLog>> loadLogList(@RequestBody PageDTO pageDTO) {
        PageVO<SystemLog> result = adminLogService.loadLogList(pageDTO);
        return ResponseVO.success(result);
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
