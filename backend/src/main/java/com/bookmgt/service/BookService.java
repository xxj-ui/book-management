package com.bookmgt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookmgt.dto.BookRequest;
import com.bookmgt.entity.Book;

import java.util.List;

public interface BookService {
    Page<Book> listBooks(int page, int size, String keyword, Long categoryId);
    Book getDetailByIsbn(String isbn);
    Book addBook(BookRequest req, Long userId);
    Book updateBook(BookRequest req);
    void deleteBook(Long id);
    List<Book> getNewBooks(int limit);
    List<Book> getHotBooks(int limit);
}
