package com.bookmgt.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.bookmgt.common.result.PageResult;
import com.bookmgt.common.result.Result;
import com.bookmgt.dto.BookRequest;
import com.bookmgt.entity.Book;
import com.bookmgt.service.BookService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Result<PageResult<Book>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        var p = bookService.listBooks(page, size, keyword, categoryId);
        return Result.success(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }

    @GetMapping("/isbn/{isbn}")
    public Result<Book> detailByIsbn(@PathVariable String isbn) {
        Book book = bookService.getDetailByIsbn(isbn);
        if (book == null) return Result.error(404, "图书不存在");
        return Result.success(book);
    }

    @PostMapping
    public Result<Book> add(@Valid @RequestBody BookRequest req) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(bookService.addBook(req, userId));
    }

    @PutMapping("/{id}")
    public Result<Book> update(@PathVariable Long id, @Valid @RequestBody BookRequest req) {
        req.setId(id);
        return Result.success(bookService.updateBook(req));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success();
    }

    @GetMapping("/new")
    public Result<List<Book>> newBooks(@RequestParam(defaultValue = "6") int limit) {
        return Result.success(bookService.getNewBooks(limit));
    }

    @GetMapping("/hot")
    public Result<List<Book>> hotBooks(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(bookService.getHotBooks(limit));
    }
}
