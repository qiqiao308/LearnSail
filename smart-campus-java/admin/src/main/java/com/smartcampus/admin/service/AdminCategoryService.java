package com.smartcampus.admin.service;

import com.smartcampus.common.po.CourseCategory;
import com.smartcampus.common.vo.CategoryVO;

import java.util.List;

public interface AdminCategoryService {

    List<CategoryVO> loadCategoryTree();

    void saveCategory(CourseCategory category);

    void updateCategory(CourseCategory category);

    void deleteCategory(Long categoryId);
}
