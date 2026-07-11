package com.smartcampus.admin.controller;

import com.smartcampus.admin.service.AdminFileService;
import com.smartcampus.common.dto.FileQueryDTO;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.FileInfoVO;
import com.smartcampus.common.vo.PageVO;
import com.smartcampus.common.vo.ResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/adminFile")
public class AdminFileController {

    @Autowired
    private AdminFileService adminFileService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/uploadFile")
    public ResponseVO<FileInfoVO> uploadFile(@RequestParam("file") MultipartFile file,
                                              HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        FileInfoVO result = adminFileService.uploadFile(file, userId);
        return ResponseVO.success(result);
    }

    @PostMapping("/loadFileList")
    public ResponseVO<PageVO<FileInfoVO>> loadFileList(@RequestBody FileQueryDTO queryDTO) {
        PageVO<FileInfoVO> result = adminFileService.loadFileList(queryDTO);
        return ResponseVO.success(result);
    }

    @DeleteMapping("/deleteFile")
    public ResponseVO<Void> deleteFile(@RequestParam Long fileId) {
        adminFileService.deleteFile(fileId);
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
