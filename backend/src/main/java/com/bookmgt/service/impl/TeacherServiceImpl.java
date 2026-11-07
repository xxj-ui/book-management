package com.bookmgt.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookmgt.dto.UserRequest;
import com.bookmgt.entity.Student;
import com.bookmgt.entity.Teacher;
import com.bookmgt.mapper.StudentMapper;
import com.bookmgt.mapper.TeacherMapper;
import com.bookmgt.service.TeacherService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherServiceImpl implements TeacherService {

    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;

    public TeacherServiceImpl(TeacherMapper teacherMapper, StudentMapper studentMapper) {
        this.teacherMapper = teacherMapper;
        this.studentMapper = studentMapper;
    }

    @Override
    public Teacher addTeacher(UserRequest req) {
        if (req.getPhone() == null || !req.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        // 教师表查学号重复
        Teacher exists = teacherMapper.selectOne(
                new LambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, req.getUserId()));
        if (exists != null) throw new IllegalArgumentException("工号已存在");
        // 学生表查手机号重复
        Teacher phoneTeacher = teacherMapper.selectOne(
                new LambdaQueryWrapper<Teacher>().eq(Teacher::getPhone, req.getPhone()));
        if (phoneTeacher != null) throw new IllegalArgumentException("手机号已存在");
        Student phoneStudent = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>().eq(Student::getPhone, req.getPhone()));
        if (phoneStudent != null) throw new IllegalArgumentException("手机号已存在");

        Teacher t = new Teacher();
        t.setUserId(req.getUserId());
        t.setPassword(BCrypt.hashpw(req.getPassword() != null ? req.getPassword() : "123456", BCrypt.gensalt()));
        t.setName(req.getName());
        t.setMajorId(req.getMajorId());
        t.setPhone(req.getPhone());
        t.setStatus(1);
        teacherMapper.insert(t);
        return t;
    }

    @Override
    public Page<Teacher> listTeachers(int page, int size, String keyword) {
        // 使用自定义查询以获取 major_name
        List<Teacher> all = teacherMapper.selectAllWithMajor();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            all = all.stream()
                    .filter(t -> (t.getName() != null && t.getName().toLowerCase().contains(kw))
                            || (t.getUserId() != null && t.getUserId().toLowerCase().contains(kw)))
                    .toList();
        }
        int total = all.size();
        int from = (page - 1) * size;
        int to = Math.min(from + size, total);
        List<Teacher> records = from >= total ? List.of() : all.subList(from, to);
        Page<Teacher> result = new Page<>(page, size, total);
        result.setRecords(records);
        return result;
    }

    @Override
    public Teacher getById(Long id) {
        return teacherMapper.selectById(id);
    }

    @Override
    public Teacher updateTeacher(UserRequest req) {
        Teacher t = teacherMapper.selectById(req.getId());
        if (t == null) throw new IllegalArgumentException("教师不存在");
        if (req.getName() != null) t.setName(req.getName());
        if (req.getPhone() != null && !req.getPhone().equals(t.getPhone())) {
            if (!req.getPhone().matches("^1[3-9]\\d{9}$")) {
                throw new IllegalArgumentException("手机号格式不正确");
            }
            Teacher phoneTeacher = teacherMapper.selectOne(
                    new LambdaQueryWrapper<Teacher>().eq(Teacher::getPhone, req.getPhone()));
            if (phoneTeacher != null) throw new IllegalArgumentException("手机号已存在");
            Student phoneStudent = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>().eq(Student::getPhone, req.getPhone()));
            if (phoneStudent != null) throw new IllegalArgumentException("手机号已存在");
            t.setPhone(req.getPhone());
        }
        if (req.getStatus() != null) t.setStatus(req.getStatus());
        teacherMapper.updateById(t);
        return t;
    }

    @Override
    public void deleteTeacher(Long id) {
        teacherMapper.deleteById(id);
    }
}
