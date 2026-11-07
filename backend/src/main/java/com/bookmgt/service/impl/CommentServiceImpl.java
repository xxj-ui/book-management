package com.bookmgt.service.impl;

import com.bookmgt.dto.CommentRequest;
import com.bookmgt.entity.Comment;
import com.bookmgt.mapper.CommentMapper;
import com.bookmgt.service.CommentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;

    public CommentServiceImpl(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    @Override
    public List<Comment> listByBookId(Long bookId) {
        return commentMapper.selectByBookId(bookId);
    }

    @Override
    public Comment addComment(CommentRequest req, Long userId) {
        Comment comment = new Comment();
        comment.setBookId(req.getBookId());
        comment.setUserId(userId);
        comment.setRating(req.getRating());
        comment.setContent(req.getContent());
        commentMapper.insert(comment);
        return commentMapper.selectByBookId(req.getBookId()).stream()
                .filter(c -> c.getId().equals(comment.getId()))
                .findFirst().orElse(comment);
    }

    @Override
    public void deleteComment(Long id) {
        commentMapper.deleteById(id);
    }
}
