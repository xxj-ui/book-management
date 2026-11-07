package com.bookmgt.controller;

import com.bookmgt.common.result.PageResult;
import com.bookmgt.common.result.Result;
import com.bookmgt.dto.UserRequest;
import com.bookmgt.entity.Teacher;
import com.bookmgt.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) { this.teacherService = teacherService; }

    @GetMapping
    public Result<PageResult<Teacher>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        var p = teacherService.listTeachers(page, size, keyword);
        return Result.success(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }

    @GetMapping("/{id}") public Result<Teacher> get(@PathVariable Long id) { return Result.success(teacherService.getById(id)); }
    @PostMapping public Result<Teacher> add(@Valid @RequestBody UserRequest req) { return Result.success(teacherService.addTeacher(req)); }
    @PutMapping("/{id}") public Result<Teacher> update(@PathVariable Long id, @RequestBody UserRequest req) { req.setId(id); return Result.success(teacherService.updateTeacher(req)); }
    @DeleteMapping("/{id}") public Result<Void> delete(@PathVariable Long id) { teacherService.deleteTeacher(id); return Result.success(); }
}
