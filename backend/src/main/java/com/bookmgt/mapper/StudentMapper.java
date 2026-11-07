package com.bookmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookmgt.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    @Select("SELECT s.*, c.name as class_name, m.name as major_name, d.name as department_name " +
            "FROM t_student s " +
            "LEFT JOIN t_class c ON s.class_id = c.id " +
            "LEFT JOIN t_major m ON c.major_id = m.id " +
            "LEFT JOIN t_department d ON m.department_id = d.id " +
            "WHERE s.user_id = #{userId}")
    Student selectByUserId(String userId);

    @Select("SELECT s.*, c.name as class_name, m.name as major_name, d.name as department_name " +
            "FROM t_student s " +
            "LEFT JOIN t_class c ON s.class_id = c.id " +
            "LEFT JOIN t_major m ON c.major_id = m.id " +
            "LEFT JOIN t_department d ON m.department_id = d.id " +
            "WHERE s.phone = #{phone}")
    Student selectByPhone(String phone);

    @Select("SELECT s.*, c.name as class_name, m.name as major_name, d.name as department_name " +
            "FROM t_student s " +
            "LEFT JOIN t_class c ON s.class_id = c.id " +
            "LEFT JOIN t_major m ON c.major_id = m.id " +
            "LEFT JOIN t_department d ON m.department_id = d.id " +
            "WHERE s.id = #{id}")
    Student selectByIdWithDetails(Long id);
}
