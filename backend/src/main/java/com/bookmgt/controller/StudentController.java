package com.bookmgt.controller;

import com.bookmgt.common.result.PageResult;
import com.bookmgt.common.result.Result;
import com.bookmgt.dto.UserRequest;
import com.bookmgt.entity.Student;
import com.bookmgt.service.StudentService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) { this.studentService = studentService; }

    @GetMapping
    public Result<PageResult<Student>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Integer educationLevel) {
        var p = studentService.listStudents(page, size, keyword, classId, educationLevel);
        return Result.success(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }

    @GetMapping("/{id}") public Result<Student> get(@PathVariable Long id) { return Result.success(studentService.getById(id)); }
    @GetMapping("/userId/{userId}") public Result<Student> getByUserId(@PathVariable String userId) { return Result.success(studentService.getByUserId(userId)); }
    @PostMapping public Result<Student> add(@Valid @RequestBody UserRequest req) { return Result.success(studentService.addStudent(req)); }
    @PutMapping("/{id}") public Result<Student> update(@PathVariable Long id, @RequestBody UserRequest req) { req.setId(id); return Result.success(studentService.updateStudent(req)); }
    @DeleteMapping("/{id}") public Result<Void> delete(@PathVariable Long id) { studentService.deleteStudent(id); return Result.success(); }
}
