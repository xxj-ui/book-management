package com.bookmgt.controller;

import com.bookmgt.common.result.Result;
import com.bookmgt.entity.*;
import com.bookmgt.mapper.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dictionary")
public class DictionaryController {

    private final DepartmentMapper departmentMapper;
    private final MajorMapper majorMapper;
    private final ClassMapper classMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CACHE_PREFIX = "cache:dictionary:";

    public DictionaryController(DepartmentMapper departmentMapper,
                                MajorMapper majorMapper,
                                ClassMapper classMapper,
                                StringRedisTemplate redisTemplate) {
        this.departmentMapper = departmentMapper;
        this.majorMapper = majorMapper;
        this.classMapper = classMapper;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/departments")
    public Result<List<Department>> listDepartments() {
        String key = CACHE_PREFIX + "departments";
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                List<Department> list = objectMapper.readValue(cached,
                        new TypeReference<List<Department>>() {});
                return Result.success(list);
            } catch (Exception ignored) {}
        }
        List<Department> list = departmentMapper.selectList(null);
        try { redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(list), 2, TimeUnit.HOURS); } catch (Exception ignored) {}
        return Result.success(list);
    }

    @GetMapping("/majors")
    public Result<List<Major>> listMajors(@RequestParam(required = false) Long departmentId) {
        String key = CACHE_PREFIX + "majors:" + (departmentId != null ? departmentId : "all");
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                List<Major> list = objectMapper.readValue(cached,
                        new TypeReference<List<Major>>() {});
                return Result.success(list);
            } catch (Exception ignored) {}
        }
        List<Major> list;
        if (departmentId != null && departmentId > 0) {
            list = majorMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Major>()
                            .eq(Major::getDepartmentId, departmentId));
        } else {
            list = majorMapper.selectList(null);
        }
        try { redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(list), 2, TimeUnit.HOURS); } catch (Exception ignored) {}
        return Result.success(list);
    }

    @GetMapping("/classes")
    public Result<List<ClassEntity>> listClasses(@RequestParam(required = false) Long majorId) {
        String key = CACHE_PREFIX + "classes:" + (majorId != null ? majorId : "all");
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                List<ClassEntity> list = objectMapper.readValue(cached,
                        new TypeReference<List<ClassEntity>>() {});
                return Result.success(list);
            } catch (Exception ignored) {}
        }
        List<ClassEntity> list;
        if (majorId != null && majorId > 0) {
            list = classMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ClassEntity>()
                            .eq(ClassEntity::getMajorId, majorId));
        } else {
            list = classMapper.selectList(null);
        }
        try { redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(list), 2, TimeUnit.HOURS); } catch (Exception ignored) {}
        return Result.success(list);
    }

    @GetMapping("/all-classes")
    public Result<?> listAllClasses() {
        String key = CACHE_PREFIX + "all-classes";
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                List<java.util.Map<String, Object>> list = objectMapper.readValue(cached,
                        new TypeReference<List<java.util.Map<String, Object>>>() {});
                return Result.success(list);
            } catch (Exception ignored) {}
        }
        List<ClassEntity> classes = classMapper.selectList(null);
        List<java.util.Map<String, Object>> result = classes.stream().map(c -> {
            Major major = majorMapper.selectById(c.getMajorId());
            Department dept = null;
            if (major != null) {
                dept = departmentMapper.selectById(major.getDepartmentId());
            }
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("classId", c.getId());
            map.put("className", c.getName());
            map.put("major", major != null ? major.getName() : "");
            map.put("department", dept != null ? dept.getName() : "");
            map.put("grade", c.getName().length() >= 2 ? "20" + c.getName().substring(0, 2) : "");
            return map;
        }).collect(Collectors.toList());
        try { redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result), 2, TimeUnit.HOURS); } catch (Exception ignored) {}
        return Result.success(result);
    }
}
