package com.bookmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookmgt.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT c.*, (SELECT COUNT(*) FROM t_book b WHERE b.category_id = c.id AND b.deleted = 0) as book_count " +
            "FROM t_category c WHERE c.deleted = 0 ORDER BY c.id")
    List<Category> selectAllWithBookCount();
}
