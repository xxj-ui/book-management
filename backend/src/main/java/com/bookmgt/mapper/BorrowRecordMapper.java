package com.bookmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookmgt.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {

    @Select("SELECT r.*, COALESCE(s.name, t.name) as user_name, " +
            "CASE WHEN t.id IS NOT NULL THEN '教师' " +
            "  WHEN s.education_level = 1 THEN '高职' " +
            "  WHEN s.education_level = 2 THEN '本科' " +
            "  WHEN s.education_level = 3 THEN '硕士' " +
            "  WHEN s.education_level = 4 THEN '博士' " +
            "  ELSE '' END as user_role_name, " +
            "b.title as book_title, b.isbn, cp.barcode, cp.status AS copy_status " +
            "FROM t_borrow_record r " +
            "LEFT JOIN t_student s ON r.user_id = s.id " +
            "LEFT JOIN t_teacher t ON r.user_id = t.id " +
            "LEFT JOIN t_book_copy cp ON r.copy_id = cp.id " +
            "LEFT JOIN t_book b ON cp.book_id = b.id " +
            "WHERE r.id = #{id}")
    BorrowRecord selectDetailById(Long id);

    @Select("SELECT r.*, COALESCE(s.name, t.name) as user_name, " +
            "CASE WHEN t.id IS NOT NULL THEN '教师' " +
            "  WHEN s.education_level = 1 THEN '高职' " +
            "  WHEN s.education_level = 2 THEN '本科' " +
            "  WHEN s.education_level = 3 THEN '硕士' " +
            "  WHEN s.education_level = 4 THEN '博士' " +
            "  ELSE '' END as user_role_name, " +
            "b.title as book_title, b.isbn, cp.barcode, cp.status AS copy_status " +
            "FROM t_borrow_record r " +
            "LEFT JOIN t_student s ON r.user_id = s.id " +
            "LEFT JOIN t_teacher t ON r.user_id = t.id " +
            "LEFT JOIN t_book_copy cp ON r.copy_id = cp.id " +
            "LEFT JOIN t_book b ON cp.book_id = b.id " +
            "WHERE r.status = #{status} ORDER BY r.create_time DESC")
    List<BorrowRecord> selectByStatus(Integer status);

    @Select("SELECT r.*, COALESCE(s.name, t.name) as user_name, " +
            "CASE WHEN t.id IS NOT NULL THEN '教师' " +
            "  WHEN s.education_level = 1 THEN '高职' " +
            "  WHEN s.education_level = 2 THEN '本科' " +
            "  WHEN s.education_level = 3 THEN '硕士' " +
            "  WHEN s.education_level = 4 THEN '博士' " +
            "  ELSE '' END as user_role_name, " +
            "b.title as book_title, b.isbn, cp.barcode, cp.status AS copy_status " +
            "FROM t_borrow_record r " +
            "LEFT JOIN t_student s ON r.user_id = s.id " +
            "LEFT JOIN t_teacher t ON r.user_id = t.id " +
            "LEFT JOIN t_book_copy cp ON r.copy_id = cp.id " +
            "LEFT JOIN t_book b ON cp.book_id = b.id " +
            "WHERE r.user_id = #{userId} ORDER BY r.create_time DESC")
    List<BorrowRecord> selectByUserId(Long userId);

    @Select("SELECT r.*, COALESCE(s.name, t.name) as user_name, " +
            "CASE WHEN t.id IS NOT NULL THEN '教师' " +
            "  WHEN s.education_level = 1 THEN '高职' " +
            "  WHEN s.education_level = 2 THEN '本科' " +
            "  WHEN s.education_level = 3 THEN '硕士' " +
            "  WHEN s.education_level = 4 THEN '博士' " +
            "  ELSE '' END as user_role_name, " +
            "b.title as book_title, b.isbn, cp.barcode, cp.status AS copy_status " +
            "FROM t_borrow_record r " +
            "LEFT JOIN t_student s ON r.user_id = s.id " +
            "LEFT JOIN t_teacher t ON r.user_id = t.id " +
            "LEFT JOIN t_book_copy cp ON r.copy_id = cp.id " +
            "LEFT JOIN t_book b ON cp.book_id = b.id " +
            "ORDER BY r.create_time DESC")
    List<BorrowRecord> selectAllWithDetail();

    @Select("SELECT r.*, COALESCE(s.name, t.name) as user_name, " +
            "CASE WHEN t.id IS NOT NULL THEN '教师' " +
            "  WHEN s.education_level = 1 THEN '高职' " +
            "  WHEN s.education_level = 2 THEN '本科' " +
            "  WHEN s.education_level = 3 THEN '硕士' " +
            "  WHEN s.education_level = 4 THEN '博士' " +
            "  ELSE '' END as user_role_name, " +
            "b.title as book_title, b.isbn, cp.barcode, cp.status AS copy_status, cp.shelf_floor, cp.shelf_zone, cp.shelf_no " +
            "FROM t_borrow_record r " +
            "LEFT JOIN t_student s ON r.user_id = s.id " +
            "LEFT JOIN t_teacher t ON r.user_id = t.id " +
            "LEFT JOIN t_book_copy cp ON r.copy_id = cp.id " +
            "LEFT JOIN t_book b ON cp.book_id = b.id " +
            "WHERE r.status = 4 AND r.return_date IS NOT NULL AND cp.status = 3 " +
            "ORDER BY r.return_date DESC, r.id DESC")
    List<BorrowRecord> selectPendingShelf();

    @Select("SELECT COUNT(*) FROM t_borrow_record WHERE copy_id = #{copyId} AND status IN (1, 3)")
    Integer countActiveByCopyId(Long copyId);

    @Select("SELECT COUNT(*) FROM t_borrow_record WHERE user_id = #{userId} AND status IN (1, 3, 4)")
    Integer countCurrentByUserId(Long userId);

    /** 统计用户当前借阅的新书数量（上架30天内） */
    @Select("SELECT COUNT(*) FROM t_borrow_record r " +
            "JOIN t_book_copy cp ON r.copy_id = cp.id " +
            "JOIN t_book b ON cp.book_id = b.id " +
            "WHERE r.user_id = #{userId} AND r.status IN (1, 3, 4) " +
            "AND b.create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)")
    Integer countCurrentNewBooksByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM t_borrow_record WHERE user_id = #{userId}")
    Integer countTotalByUserId(Long userId);

    @Update("UPDATE t_borrow_record SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
