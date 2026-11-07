package com.bookmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookmgt.dto.CategoryRequest;
import com.bookmgt.entity.Book;
import com.bookmgt.entity.Category;
import com.bookmgt.mapper.BookMapper;
import com.bookmgt.mapper.CategoryMapper;
import com.bookmgt.service.CategoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final String CACHE_KEY = "cache:categories";
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CategoryMapper categoryMapper;
    private final BookMapper bookMapper;
    private final StringRedisTemplate redisTemplate;

    public CategoryServiceImpl(CategoryMapper categoryMapper, BookMapper bookMapper,
                                StringRedisTemplate redisTemplate) {
        this.categoryMapper = categoryMapper;
        this.bookMapper = bookMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void clearCategoryCache() {
        try { redisTemplate.delete(CACHE_KEY); } catch (Exception ignored) {}
    }

    @Override
    public List<Category> listAll() {
        // 先从缓存读取
        String cached = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<Category>>() {});
            } catch (Exception ignored) {}
        }
        // 缓存未命中，查数据库
        List<Category> categories = categoryMapper.selectAllWithBookCount();
        for (Category c : categories) {
            if (c.getBookCount() == null) {
                Long count = bookMapper.selectCount(new LambdaQueryWrapper<Book>().eq(Book::getCategoryId, c.getId()));
                c.setBookCount(count.intValue());
            }
        }
        // 写入缓存（1小时过期）
        try {
            redisTemplate.opsForValue().set(CACHE_KEY, objectMapper.writeValueAsString(categories), 1, java.util.concurrent.TimeUnit.HOURS);
        } catch (Exception ignored) {}
        return categories;
    }

    @Override
    public Page<Category> listPage(int page, int size, String keyword) {
        Page<Category> p = new Page<>(page, size);
        LambdaQueryWrapper<Category> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.like(Category::getName, keyword);
        }
        qw.orderByAsc(Category::getId);
        Page<Category> result = categoryMapper.selectPage(p, qw);
        for (Category c : result.getRecords()) {
            Long count = bookMapper.selectCount(new LambdaQueryWrapper<Book>().eq(Book::getCategoryId, c.getId()));
            c.setBookCount(count.intValue());
        }
        return result;
    }

    @Override
    public Category addCategory(CategoryRequest req) {
        // 检查是否已存在
        Long exists = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>().eq(Category::getName, req.getName()));
        if (exists > 0) throw new IllegalArgumentException("分类名称已存在");

        Category cat = new Category();
        cat.setName(req.getName());
        cat.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        categoryMapper.insert(cat);
        this.clearCategoryCache();
        return cat;
    }

    @Override
    public Category updateCategory(CategoryRequest req) {
        Category cat = categoryMapper.selectById(req.getId());
        if (cat == null) throw new IllegalArgumentException("分类不存在");
        if (req.getName() != null) cat.setName(req.getName());
        if (req.getStatus() != null) {
            // 如果要禁用，检查是否有图书
            if (req.getStatus() == 0) {
                Long bookCount = bookMapper.selectCount(
                        new LambdaQueryWrapper<Book>().eq(Book::getCategoryId, req.getId()));
                if (bookCount > 0) throw new IllegalArgumentException("该分类下有图书，无法禁用");
            }
            cat.setStatus(req.getStatus());
        }
        categoryMapper.updateById(cat);
        this.clearCategoryCache();
        return cat;
    }

    @Override
    public void deleteCategory(Long id) {
        Long bookCount = bookMapper.selectCount(
                new LambdaQueryWrapper<Book>().eq(Book::getCategoryId, id));
        if (bookCount > 0) throw new IllegalArgumentException("该分类下有图书，无法删除");
        categoryMapper.deleteById(id);
        this.clearCategoryCache();
    }
}
