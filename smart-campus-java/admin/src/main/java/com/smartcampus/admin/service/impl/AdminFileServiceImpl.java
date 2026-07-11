package com.smartcampus.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.admin.service.AdminFileService;
import com.smartcampus.common.dto.FileQueryDTO;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.mapper.FileInfoMapper;
import com.smartcampus.common.po.FileInfo;
import com.smartcampus.common.service.FileStorageService;
import com.smartcampus.common.vo.FileInfoVO;
import com.smartcampus.common.vo.PageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminFileServiceImpl implements AdminFileService {

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private FileStorageService fileStorageService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public FileInfoVO uploadFile(MultipartFile file, Long userId) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "上传文件不能为空");
        }

        // store file
        String filePath = fileStorageService.store(file);
        long fileSize = fileStorageService.getFileSize(filePath);

        // save file info record
        FileInfo fileInfo = new FileInfo();
        fileInfo.setOriginalName(file.getOriginalFilename());
        fileInfo.setStoredName(filePath.substring(filePath.lastIndexOf('/') + 1));
        fileInfo.setFilePath(filePath);
        fileInfo.setFileSize(fileSize);
        fileInfo.setFileType(getFileExtension(file.getOriginalFilename()));
        fileInfo.setMimeType(file.getContentType());
        fileInfo.setUploaderId(userId);
        fileInfo.setCreateTime(LocalDateTime.now());
        fileInfoMapper.insert(fileInfo);

        // build response
        FileInfoVO vo = new FileInfoVO();
        BeanUtils.copyProperties(fileInfo, vo);
        if (fileInfo.getCreateTime() != null) {
            vo.setCreateTime(fileInfo.getCreateTime().format(FORMATTER));
        }
        vo.setUrl("/upload/" + fileInfo.getStoredName());
        return vo;
    }

    @Override
    public PageVO<FileInfoVO> loadFileList(FileQueryDTO queryDTO) {
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getFileType() != null && !queryDTO.getFileType().isBlank()) {
            wrapper.eq(FileInfo::getFileType, queryDTO.getFileType());
        }
        wrapper.orderByDesc(FileInfo::getCreateTime);

        Page<FileInfo> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<FileInfo> result = fileInfoMapper.selectPage(page, wrapper);

        List<FileInfoVO> voList = result.getRecords().stream().map(fileInfo -> {
            FileInfoVO vo = new FileInfoVO();
            BeanUtils.copyProperties(fileInfo, vo);
            if (fileInfo.getCreateTime() != null) {
                vo.setCreateTime(fileInfo.getCreateTime().format(FORMATTER));
            }
            vo.setUrl("/upload/" + fileInfo.getStoredName());
            return vo;
        }).collect(Collectors.toList());

        PageVO<FileInfoVO> pageVO = new PageVO<>();
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

    @Override
    public void deleteFile(Long fileId) {
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);
        if (fileInfo == null) {
            throw new BusinessException(404, "文件不存在");
        }
        // delete physical file
        fileStorageService.delete(fileInfo.getFilePath());
        // delete record
        fileInfoMapper.deleteById(fileId);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
