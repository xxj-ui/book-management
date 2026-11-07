package com.bookmgt.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookmgt.dto.UserRequest;
import com.bookmgt.entity.*;
import com.bookmgt.mapper.*;
import com.bookmgt.service.StudentService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;
    private final ClassMapper classMapper;
    private final MajorMapper majorMapper;
    private final DepartmentMapper departmentMapper;
    private final BorrowRecordMapper borrowRecordMapper;

    private static final Map<Integer, String> EDU_MAP = Map.of(1, "高职", 2, "本科", 3, "硕士", 4, "博士");

    public StudentServiceImpl(StudentMapper studentMapper, ClassMapper classMapper,
                              MajorMapper majorMapper, DepartmentMapper departmentMapper,
                              BorrowRecordMapper borrowRecordMapper) {
        this.studentMapper = studentMapper;
        this.classMapper = classMapper;
        this.majorMapper = majorMapper;
        this.departmentMapper = departmentMapper;
        this.borrowRecordMapper = borrowRecordMapper;
    }

    @Override
    public Page<Student> listStudents(int page, int size, String keyword, Long classId, Integer educationLevel) {
        Page<Student> p = new Page<>(page, size);
        LambdaQueryWrapper<Student> qw = new LambdaQueryWrapper<Student>()
                .eq(classId != null && classId > 0, Student::getClassId, classId)
                .eq(educationLevel != null && educationLevel > 0, Student::getEducationLevel, educationLevel);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(Student::getName, keyword).or().like(Student::getUserId, keyword));
        }
        qw.orderByDesc(Student::getId);
        Page<Student> result = studentMapper.selectPage(p, qw);
        for (Student s : result.getRecords()) enrich(s);
        return result;
    }

    @Override
    public Student getByUserId(String userId) {
        Student s = studentMapper.selectByUserId(userId);
        if (s != null) enrich(s);
        return s;
    }

    @Override
    public Student getById(Long id) {
        Student s = studentMapper.selectById(id);
        if (s != null) enrich(s);
        return s;
    }

    @Override
    public Student addStudent(UserRequest req) {
        if (req.getPhone() == null || !req.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        Student exists = studentMapper.selectByUserId(req.getUserId());
        if (exists != null) throw new IllegalArgumentException("学号已存在");
        Student phoneExists = studentMapper.selectByPhone(req.getPhone());
        if (phoneExists != null) throw new IllegalArgumentException("手机号已存在");
        Student s = new Student();
        s.setUserId(req.getUserId());
        s.setPassword(BCrypt.hashpw(req.getPassword() != null ? req.getPassword() : "123456", BCrypt.gensalt()));
        s.setName(req.getName());
        s.setEducationLevel(req.getEducationLevel());
        s.setClassId(req.getClassId());
        s.setPhone(req.getPhone());
        s.setStatus(1);
        studentMapper.insert(s);
        return s;
    }

    @Override
    public Student updateStudent(UserRequest req) {
        Student s = studentMapper.selectById(req.getId());
        if (s == null) throw new IllegalArgumentException("学生不存在");
        if (req.getUserId() != null && !req.getUserId().equals(s.getUserId())) {
            Student exists = studentMapper.selectByUserId(req.getUserId());
            if (exists != null) throw new IllegalArgumentException("学号已存在");
            s.setUserId(req.getUserId());
        }
        if (req.getName() != null) s.setName(req.getName());
        if (req.getEducationLevel() != null) s.setEducationLevel(req.getEducationLevel());
        if (req.getClassId() != null) s.setClassId(req.getClassId());
        if (req.getPhone() != null && !req.getPhone().equals(s.getPhone())) {
            if (!req.getPhone().matches("^1[3-9]\\d{9}$")) {
                throw new IllegalArgumentException("手机号格式不正确");
            }
            Student phoneExists = studentMapper.selectByPhone(req.getPhone());
            if (phoneExists != null) throw new IllegalArgumentException("手机号已存在");
            s.setPhone(req.getPhone());
        }
        if (req.getStatus() != null) s.setStatus(req.getStatus());
        studentMapper.updateById(s);
        return s;
    }

    @Override
    public void deleteStudent(Long id) {
        studentMapper.deleteById(id);
    }

    private void enrich(Student s) {
        Integer count = borrowRecordMapper.countCurrentByUserId(s.getId());
        s.setCurrentBorrows(count != null ? count : 0);
        Integer total = borrowRecordMapper.countTotalByUserId(s.getId());
        s.setTotalBorrows(total != null ? total : 0);
        if (s.getEducationLevel() != null) {
            s.setEducationLevelName(EDU_MAP.getOrDefault(s.getEducationLevel(), ""));
        }
        if (s.getClassId() != null) {
            ClassEntity cls = classMapper.selectById(s.getClassId());
            if (cls != null) {
                s.setClassName(cls.getName());
                Major mj = majorMapper.selectById(cls.getMajorId());
                if (mj != null) {
                    s.setMajorName(mj.getName());
                    Department dept = departmentMapper.selectById(mj.getDepartmentId());
                    if (dept != null) s.setDepartmentName(dept.getName());
                }
            }
        }
    }
}
