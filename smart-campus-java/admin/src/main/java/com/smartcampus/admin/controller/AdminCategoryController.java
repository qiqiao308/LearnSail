package com.smartcampus.admin.controller;

import com.smartcampus.admin.service.AdminCategoryService;
import com.smartcampus.common.po.CourseCategory;
import com.smartcampus.common.utils.JwtUtil;
import com.smartcampus.common.vo.CategoryVO;
import com.smartcampus.common.vo.ResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adminCategory")
public class AdminCategoryController {

    @Autowired
    private AdminCategoryService adminCategoryService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/loadCategoryTree")
    public ResponseVO<List<CategoryVO>> loadCategoryTree() {
        List<CategoryVO> result = adminCategoryService.loadCategoryTree();
        return ResponseVO.success(result);
    }

    @PostMapping("/saveCategory")
    public ResponseVO<Void> saveCategory(@RequestBody CourseCategory category) {
        adminCategoryService.saveCategory(category);
        return ResponseVO.success(null);
    }

    @PutMapping("/updateCategory")
    public ResponseVO<Void> updateCategory(@RequestBody CourseCategory category) {
        adminCategoryService.updateCategory(category);
        return ResponseVO.success(null);
    }

    @DeleteMapping("/deleteCategory")
    public ResponseVO<Void> deleteCategory(@RequestParam Long categoryId) {
        adminCategoryService.deleteCategory(categoryId);
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
