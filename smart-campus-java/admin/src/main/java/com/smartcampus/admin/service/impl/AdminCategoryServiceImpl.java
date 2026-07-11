package com.smartcampus.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcampus.admin.service.AdminCategoryService;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.mapper.CourseCategoryMapper;
import com.smartcampus.common.po.CourseCategory;
import com.smartcampus.common.vo.CategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminCategoryServiceImpl implements AdminCategoryService {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CategoryVO> loadCategoryTree() {
        List<CourseCategory> allCategories = courseCategoryMapper.selectList(
                new LambdaQueryWrapper<CourseCategory>().orderByAsc(CourseCategory::getSortOrder));

        List<CategoryVO> rootList = new ArrayList<>();
        for (CourseCategory category : allCategories) {
            if (category.getParentId() == null || category.getParentId() == 0) {
                CategoryVO vo = toVO(category);
                vo.setChildren(buildChildren(category.getId(), allCategories));
                rootList.add(vo);
            }
        }
        return rootList;
    }

    private List<CategoryVO> buildChildren(Long parentId, List<CourseCategory> allCategories) {
        List<CategoryVO> children = new ArrayList<>();
        for (CourseCategory category : allCategories) {
            if (parentId.equals(category.getParentId())) {
                CategoryVO vo = toVO(category);
                vo.setChildren(buildChildren(category.getId(), allCategories));
                children.add(vo);
            }
        }
        return children;
    }

    private CategoryVO toVO(CourseCategory category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }

    @Override
    public void saveCategory(CourseCategory category) {
        category.setId(null);
        category.setCreateTime(LocalDateTime.now());
        courseCategoryMapper.insert(category);
    }

    @Override
    public void updateCategory(CourseCategory category) {
        if (category.getId() == null) {
            throw new BusinessException(400, "分类ID不能为空");
        }
        CourseCategory existing = courseCategoryMapper.selectById(category.getId());
        if (existing == null) {
            throw new BusinessException(404, "分类不存在");
        }
        courseCategoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        CourseCategory category = courseCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        // check no child categories exist
        LambdaQueryWrapper<CourseCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseCategory::getParentId, categoryId);
        Long childCount = courseCategoryMapper.selectCount(wrapper);
        if (childCount > 0) {
            throw new BusinessException(400, "该分类下存在子分类，无法删除");
        }
        courseCategoryMapper.deleteById(categoryId);
    }
}
