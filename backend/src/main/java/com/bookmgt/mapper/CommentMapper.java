package com.bookmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookmgt.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT c.*, u.name as user_name, " +
            "  CASE u.education_level " +
            "    WHEN 1 THEN '高职' WHEN 2 THEN '本科' " +
            "    WHEN 3 THEN '硕士' WHEN 4 THEN '博士' " +
            "    ELSE '' END as user_role_name, " +
            "b.isbn " +
            "FROM t_comment c " +
            "LEFT JOIN t_student u ON c.user_id = u.id " +
            "LEFT JOIN t_book b ON c.book_id = b.id " +
            "WHERE c.book_id = #{bookId} ORDER BY c.create_time DESC")
    List<Comment> selectByBookId(Long bookId);
}
