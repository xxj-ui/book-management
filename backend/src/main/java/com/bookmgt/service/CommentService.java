package com.bookmgt.service;

import com.bookmgt.dto.CommentRequest;
import com.bookmgt.entity.Comment;

import java.util.List;

public interface CommentService {
    List<Comment> listByBookId(Long bookId);
    Comment addComment(CommentRequest req, Long userId);
    void deleteComment(Long id);
}
