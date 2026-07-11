package com.smartcampus.admin.controller;

import com.smartcampus.admin.service.AdminChapterService;
import com.smartcampus.common.dto.ChapterSaveDTO;
import com.smartcampus.common.dto.SectionSaveDTO;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.ChapterVO;
import com.smartcampus.common.vo.ResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adminChapter")
public class AdminChapterController {

    @Autowired
    private AdminChapterService adminChapterService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/loadChapterList")
    public ResponseVO<List<ChapterVO>> loadChapterList(@RequestParam Long courseId) {
        List<ChapterVO> result = adminChapterService.loadChapterList(courseId);
        return ResponseVO.success(result);
    }

    @PostMapping("/saveChapter")
    public ResponseVO<Void> saveChapter(@RequestBody @Valid ChapterSaveDTO dto) {
        adminChapterService.saveChapter(dto);
        return ResponseVO.success(null);
    }

    @PutMapping("/updateChapter")
    public ResponseVO<Void> updateChapter(@RequestBody ChapterSaveDTO dto) {
        adminChapterService.updateChapter(dto);
        return ResponseVO.success(null);
    }

    @DeleteMapping("/deleteChapter")
    public ResponseVO<Void> deleteChapter(@RequestParam Long chapterId) {
        adminChapterService.deleteChapter(chapterId);
        return ResponseVO.success(null);
    }

    @PostMapping("/saveSection")
    public ResponseVO<Void> saveSection(@RequestBody @Valid SectionSaveDTO dto) {
        adminChapterService.saveSection(dto);
        return ResponseVO.success(null);
    }

    @PutMapping("/updateSection")
    public ResponseVO<Void> updateSection(@RequestBody SectionSaveDTO dto) {
        adminChapterService.updateSection(dto);
        return ResponseVO.success(null);
    }

    @DeleteMapping("/deleteSection")
    public ResponseVO<Void> deleteSection(@RequestParam Long sectionId) {
        adminChapterService.deleteSection(sectionId);
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
