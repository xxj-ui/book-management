package com.bookmgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_recommend")
public class Recommend {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bookId;
    private Integer sortOrder;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String bookTitle;
    @TableField(exist = false)
    private String bookAuthor;
    @TableField(exist = false)
    private String bookIsbn;
    @TableField(exist = false)
    private String coverUrl;
}
