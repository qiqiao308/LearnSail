package com.smartcampus.web.controller;

import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.po.FileInfo;
import com.smartcampus.common.service.FileStorageService;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.FileInfoVO;
import com.smartcampus.common.vo.ResponseVO;
import com.smartcampus.web.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/uploadFile")
    public ResponseVO<FileInfoVO> uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return ResponseVO.success(fileService.uploadFile(file, userId));
    }

    @GetMapping("/downloadFile/{fileId}")
    public void downloadFile(@PathVariable Long fileId, HttpServletResponse response) {
        FileInfo fileInfo = fileService.getFileInfo(fileId);
        if (fileInfo == null) {
            throw new BusinessException(400, "文件不存在");
        }

        try (InputStream inputStream = fileStorageService.getFileStream(fileInfo.getFilePath());
             OutputStream outputStream = response.getOutputStream()) {

            response.setContentType(fileInfo.getMimeType());
            String encodedFileName = URLEncoder.encode(fileInfo.getOriginalName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encodedFileName + "\"");
            response.setHeader("Content-Length", String.valueOf(fileInfo.getFileSize()));

            StreamUtils.copy(inputStream, outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new BusinessException(500, "文件下载失败: " + e.getMessage());
        }
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
