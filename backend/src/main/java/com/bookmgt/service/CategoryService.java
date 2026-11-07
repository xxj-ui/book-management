package com.bookmgt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookmgt.dto.CategoryRequest;
import com.bookmgt.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> listAll();
    Page<Category> listPage(int page, int size, String keyword);
    Category addCategory(CategoryRequest req);
    Category updateCategory(CategoryRequest req);
    void deleteCategory(Long id);
    void clearCategoryCache();
}
