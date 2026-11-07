package com.bookmgt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookmgt.dto.UserRequest;
import com.bookmgt.entity.Student;

public interface StudentService {
    Page<Student> listStudents(int page, int size, String keyword, Long classId, Integer educationLevel);
    Student getByUserId(String userId);
    Student getById(Long id);
    Student addStudent(UserRequest req);
    Student updateStudent(UserRequest req);
    void deleteStudent(Long id);
}
