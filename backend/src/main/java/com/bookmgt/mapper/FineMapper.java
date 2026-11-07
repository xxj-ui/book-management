package com.bookmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookmgt.entity.Fine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FineMapper extends BaseMapper<Fine> {

    /** 查询指定用户的未支付滞纳金 */
    @Select("SELECT f.* FROM t_fine f " +
            "JOIN t_borrow_record r ON f.record_id = r.id " +
            "WHERE r.user_id = #{userId} AND f.status = 1 AND f.deleted = 0")
    List<Fine> selectUnpaidByUserId(Long userId);
}
