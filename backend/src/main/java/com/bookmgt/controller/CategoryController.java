package com.bookmgt.controller;

import com.bookmgt.common.result.Result;
import com.bookmgt.dto.CategoryRequest;
import com.bookmgt.entity.Category;
import com.bookmgt.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryService.listAll());
    }

    @PostMapping
    public Result<Category> add(@RequestBody CategoryRequest req) {
        return Result.success(categoryService.addCategory(req));
    }

    @PutMapping("/{id}")
    public Result<Category> update(@PathVariable Long id, @RequestBody CategoryRequest req) {
        req.setId(id);
        return Result.success(categoryService.updateCategory(req));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}
