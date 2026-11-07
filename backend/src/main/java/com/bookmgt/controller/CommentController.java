package com.bookmgt.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.bookmgt.common.result.Result;
import com.bookmgt.dto.CommentRequest;
import com.bookmgt.entity.Comment;
import com.bookmgt.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/book/{bookId}")
    public Result<List<Comment>> listByBook(@PathVariable Long bookId) {
        return Result.success(commentService.listByBookId(bookId));
    }

    @PostMapping
    public Result<Comment> add(@Valid @RequestBody CommentRequest req) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(commentService.addComment(req, userId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success();
    }
}
