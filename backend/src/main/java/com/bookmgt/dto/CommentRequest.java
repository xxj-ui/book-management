package com.bookmgt.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评论请求
 */
@Data
public class CommentRequest {
    @NotNull
    private Long bookId;

    @Min(1) @Max(5)
    private Integer rating;

    @NotBlank
    private String content;
}
