package com.bookmgt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookmgt.dto.UserRequest;
import com.bookmgt.entity.Teacher;

public interface TeacherService {
    Page<Teacher> listTeachers(int page, int size, String keyword);
    Teacher getById(Long id);
    Teacher updateTeacher(UserRequest req);
    void deleteTeacher(Long id);
    Teacher addTeacher(UserRequest req);
}
