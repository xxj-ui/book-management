package com.bookmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookmgt.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

    @Select("SELECT t.*, m.name AS major_name, d.name AS department_name FROM t_teacher t " +
            "LEFT JOIN t_major m ON t.major_id = m.id " +
            "LEFT JOIN t_department d ON m.department_id = d.id " +
            "WHERE t.phone = #{phone}")
    Teacher selectByPhone(String phone);

    @Select("SELECT t.*, m.name AS major_name, d.name AS department_name FROM t_teacher t " +
            "LEFT JOIN t_major m ON t.major_id = m.id " +
            "LEFT JOIN t_department d ON m.department_id = d.id " +
            "WHERE t.user_id = #{userId}")
    Teacher selectByUserId(String userId);

    @Select("SELECT t.*, m.name AS major_name, d.name AS department_name FROM t_teacher t " +
            "LEFT JOIN t_major m ON t.major_id = m.id " +
            "LEFT JOIN t_department d ON m.department_id = d.id " +
            "ORDER BY t.id")
    java.util.List<Teacher> selectAllWithMajor();
}
