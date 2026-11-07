package com.bookmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookmgt.entity.BookCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BookCopyMapper extends BaseMapper<BookCopy> {

    @Select("SELECT * FROM t_book_copy WHERE book_id = #{bookId} AND status = 1 AND deleted = 0 LIMIT 1")
    BookCopy selectAvailableCopy(Long bookId);

    @Select("SELECT * FROM t_book_copy WHERE book_id = #{bookId} AND deleted = 0 AND status != 4 ORDER BY id")
    List<BookCopy> selectByBookId(@Param("bookId") Long bookId);

    @Select("SELECT cp.*, (SELECT COALESCE(s.name, t.name) FROM t_borrow_record br " +
            "LEFT JOIN t_student s ON br.user_id = s.id " +
            "LEFT JOIN t_teacher t ON br.user_id = t.id " +
            "WHERE br.copy_id = cp.id AND br.status IN (1,3) LIMIT 1) AS borrower_name " +
            "FROM t_book_copy cp WHERE cp.book_id = #{bookId} AND cp.deleted = 0 AND cp.status != 4 ORDER BY cp.id")
    List<BookCopy> selectByBookIdWithBorrower(@Param("bookId") Long bookId);

    @Select("SELECT COUNT(*) FROM t_book_copy WHERE book_id = #{bookId} AND deleted = 0 AND status != 4")
    Integer countByBookId(Long bookId);

    @Select("SELECT COUNT(*) FROM t_book_copy WHERE book_id = #{bookId} AND status = 1 AND deleted = 0")
    Integer countAvailableByBookId(Long bookId);
}
