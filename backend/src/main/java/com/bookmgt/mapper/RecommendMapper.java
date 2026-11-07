package com.bookmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookmgt.entity.Recommend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RecommendMapper extends BaseMapper<Recommend> {

    @Select("SELECT r.*, b.title AS book_title, b.author AS book_author, b.isbn AS book_isbn, b.cover_url AS cover_url " +
            "FROM t_recommend r LEFT JOIN t_book b ON r.book_id = b.id " +
            "WHERE r.deleted = 0 ORDER BY r.sort_order")
    List<Recommend> selectAllWithBook();
}
