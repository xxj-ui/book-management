package com.bookmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookmgt.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BookMapper extends BaseMapper<Book> {

    @Select("SELECT b.*, c.name as category_name, " +
            "(SELECT COUNT(*) FROM t_book_copy bc WHERE bc.book_id = b.id) as total_copies, " +
            "(SELECT COUNT(*) FROM t_book_copy bc WHERE bc.book_id = b.id AND bc.status = 1) as available_copies, " +
            "(SELECT COUNT(*) FROM t_borrow_record br WHERE br.copy_id IN (SELECT bc2.id FROM t_book_copy bc2 WHERE bc2.book_id = b.id)) as total_borrows " +
            "FROM t_book b LEFT JOIN t_category c ON b.category_id = c.id " +
            "WHERE b.id = #{id} AND b.deleted = 0")
    Book selectDetailById(Long id);

    @Select("SELECT b.*, c.name as category_name, " +
            "(SELECT COUNT(*) FROM t_book_copy bc WHERE bc.book_id = b.id) as total_copies, " +
            "(SELECT COUNT(*) FROM t_book_copy bc WHERE bc.book_id = b.id AND bc.status = 1) as available_copies, " +
            "(SELECT COUNT(*) FROM t_borrow_record br WHERE br.copy_id IN (SELECT bc2.id FROM t_book_copy bc2 WHERE bc2.book_id = b.id)) as total_borrows " +
            "FROM t_book b LEFT JOIN t_category c ON b.category_id = c.id " +
            "WHERE b.deleted = 0 " +
            "ORDER BY total_borrows DESC")
    List<Book> selectAllWithBorrows();

    @Select("SELECT b.*, c.name as category_name, " +
            "(SELECT COUNT(*) FROM t_book_copy bc WHERE bc.book_id = b.id) as total_copies, " +
            "(SELECT COUNT(*) FROM t_book_copy bc WHERE bc.book_id = b.id AND bc.status = 1) as available_copies, " +
            "(SELECT COUNT(*) FROM t_borrow_record br WHERE br.copy_id IN (SELECT bc2.id FROM t_book_copy bc2 WHERE bc2.book_id = b.id)) as total_borrows " +
            "FROM t_book b LEFT JOIN t_category c ON b.category_id = c.id " +
            "WHERE b.isbn = #{isbn} AND b.deleted = 0")
    Book selectDetailByIsbn(String isbn);

    @Update("UPDATE t_book SET cover_url = #{coverUrl} WHERE id = #{id}")
    int updateCoverUrl(@Param("id") Long id, @Param("coverUrl") String coverUrl);
}
