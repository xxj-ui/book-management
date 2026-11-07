package com.bookmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookmgt.dto.BookRequest;
import com.bookmgt.dto.CopyRequest;
import com.bookmgt.entity.Book;
import com.bookmgt.entity.BookCopy;
import com.bookmgt.entity.Category;
import com.bookmgt.mapper.BookCopyMapper;
import com.bookmgt.mapper.BookMapper;
import com.bookmgt.mapper.CategoryMapper;
import com.bookmgt.service.BookService;
import com.bookmgt.service.CategoryService;
import com.bookmgt.util.DateUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final BookCopyMapper bookCopyMapper;
    private final CategoryMapper categoryMapper;
    private final CategoryService categoryService;
    private final StringRedisTemplate redisTemplate;

    private static final String HOT_BOOKS_KEY = "hot_books";
    private static final Logger log = LoggerFactory.getLogger(BookServiceImpl.class);

    public BookServiceImpl(BookMapper bookMapper, BookCopyMapper bookCopyMapper,
                           CategoryMapper categoryMapper, CategoryService categoryService,
                           StringRedisTemplate redisTemplate) {
        this.bookMapper = bookMapper;
        this.bookCopyMapper = bookCopyMapper;
        this.categoryMapper = categoryMapper;
        this.categoryService = categoryService;
        this.redisTemplate = redisTemplate;
    }

    /** 启动时从数据库初始化热门图书 ZSET */
    @PostConstruct
    public void initHotBooks() {
        try {
            redisTemplate.delete(HOT_BOOKS_KEY);
            List<Book> allBooks = bookMapper.selectAllWithBorrows();
            for (Book b : allBooks) {
                int score = b.getTotalBorrows() != null ? b.getTotalBorrows() : 0;
                redisTemplate.opsForZSet().add(HOT_BOOKS_KEY,
                        String.valueOf(b.getId()), score);
            }
            log.info("热门图书 ZSET 初始化完成，共 {} 本图书", allBooks.size());
        } catch (Exception e) {
            log.error("热门图书 ZSET 初始化失败", e);
        }
    }

    /** 借书时增加热门图书分值 */
    public void incrementHotBook(Long bookId) {
        redisTemplate.opsForZSet().incrementScore(HOT_BOOKS_KEY, String.valueOf(bookId), 1);
    }

    @Override
    public Page<Book> listBooks(int page, int size, String keyword, Long categoryId) {
        Page<Book> p = new Page<>(page, size);
        LambdaQueryWrapper<Book> qw = new LambdaQueryWrapper<Book>()
                .eq(categoryId != null && categoryId > 0, Book::getCategoryId, categoryId);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(Book::getTitle, keyword)
                    .or().like(Book::getAuthor, keyword)
                    .or().like(Book::getIsbn, keyword));
        }
        qw.orderByDesc(Book::getId);
        Page<Book> result = bookMapper.selectPage(p, qw);
        // 填充额外字段
        for (Book book : result.getRecords()) {
            enrichBook(book);
        }
        return result;
    }

    @Override
    public Book getDetailByIsbn(String isbn) {
        Book book = bookMapper.selectDetailByIsbn(isbn);
        if (book != null) {
            book.setIsNew(DateUtil.isWithinDefaultDays(book.getCreateTime()));
            List<BookCopy> copies = bookCopyMapper.selectByBookId(book.getId());
            long available = copies.stream().filter(c -> c.getStatus() == 1).count();
            book.setTotalCopies(copies.size());
            book.setAvailableCopies((int) available);
        }
        return book;
    }

    @Override
    @Transactional
    public Book addBook(BookRequest req, Long userId) {
        // ISBN 重复检查
        Book exists = bookMapper.selectOne(new LambdaQueryWrapper<Book>().eq(Book::getIsbn, req.getIsbn()));
        if (exists != null) throw new IllegalArgumentException("ISBN 已存在");
        Book book = new Book();
        book.setIsbn(req.getIsbn());
        book.setTitle(req.getTitle());
        book.setAuthor(req.getAuthor());
        book.setPublisher(req.getPublisher());
        book.setPublishYear(req.getPublishYear());
        book.setCategoryId(req.getCategoryId());
        book.setDescription(req.getDescription());
        book.setCoverUrl(req.getCoverUrl() != null ? req.getCoverUrl() : "");
        book.setStatus(1);
        bookMapper.insert(book);

        // 添加副本
        if (req.getCopies() != null && !req.getCopies().isEmpty()) {
            for (CopyRequest cr : req.getCopies()) {
                BookCopy copy = new BookCopy();
                copy.setBookId(book.getId());
                copy.setBarcode(cr.getBarcode());
                copy.setShelfFloor(cr.getShelfFloor());
                copy.setShelfZone(cr.getShelfZone());
                copy.setShelfNo(cr.getShelfNo());
                copy.setStatus(1);
                copy.setPurchaseDate(cr.getPurchaseDate() != null ? cr.getPurchaseDate() : LocalDate.now());
                bookCopyMapper.insert(copy);
            }
        }

        // 新增图书可能影响分类统计，清除分类缓存
        categoryService.clearCategoryCache();
        return book;
    }

    @Override
    @Transactional
    public Book updateBook(BookRequest req) {
        Book book = bookMapper.selectById(req.getId());
        if (book == null) throw new IllegalArgumentException("图书不存在");

        // ISBN 重复检查（改了才查）
        if (req.getIsbn() != null && !req.getIsbn().equals(book.getIsbn())) {
            Book exists = bookMapper.selectOne(new LambdaQueryWrapper<Book>().eq(Book::getIsbn, req.getIsbn()));
            if (exists != null) throw new IllegalArgumentException("ISBN 已存在");
        }

        book.setIsbn(req.getIsbn());
        book.setTitle(req.getTitle());
        book.setAuthor(req.getAuthor());
        book.setPublisher(req.getPublisher());
        book.setPublishYear(req.getPublishYear());
        book.setCategoryId(req.getCategoryId());
        book.setDescription(req.getDescription());
        book.setCoverUrl(req.getCoverUrl() != null ? req.getCoverUrl() : book.getCoverUrl());
        book.setStatus(req.getStatus() != null ? req.getStatus() : book.getStatus());
        bookMapper.updateById(book);

        // 更新图书可能改变了分类归属，清除分类缓存
        categoryService.clearCategoryCache();
        return book;
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        // 如果有副本则不允许删除
        int copyCount = bookCopyMapper.countByBookId(id);
        if (copyCount > 0) {
            throw new IllegalArgumentException("该图书存在副本，无法删除。请先删除所有副本后再试。");
        }
        bookMapper.update(null, new LambdaUpdateWrapper<Book>()
                .eq(Book::getId, id)
                .set(Book::getDeleted, 1));

        // 删除图书可能影响分类统计，清除分类缓存
        categoryService.clearCategoryCache();
    }

    @Override
    public List<Book> getNewBooks(int limit) {
        LocalDate threshold = LocalDate.now().minusDays(30);
        LambdaQueryWrapper<Book> qw = new LambdaQueryWrapper<Book>()
                .ge(Book::getCreateTime, threshold.atStartOfDay())
                .orderByDesc(Book::getId)
                .last("LIMIT " + limit);
        List<Book> books = bookMapper.selectList(qw);
        books.forEach(this::enrichBook);
        return books;
    }

    @Override
    public List<Book> getHotBooks(int limit) {
        // 从 Redis ZSET 获取热门图书 ID
        Set<String> topIds = redisTemplate.opsForZSet()
                .reverseRange(HOT_BOOKS_KEY, 0, limit - 1);
        if (topIds == null || topIds.isEmpty()) {
            // ZSET 为空时降级为数据库查询（直接用 SQL 排序）
            log.warn("热门图书 ZSET 为空，降级为数据库查询");
            List<Book> books = bookMapper.selectAllWithBorrows();
            books.forEach(this::enrichBook);
            return books.size() > limit ? books.subList(0, limit) : books;
        }
        List<Book> result = new ArrayList<>();
        for (String id : topIds) {
            Book book = bookMapper.selectDetailById(Long.parseLong(id));
            if (book != null) { enrichBook(book); result.add(book); }
        }
        return result;
    }

    private void enrichBook(Book book) {
        if (book.getCategoryId() != null) {
            Category cat = categoryMapper.selectById(book.getCategoryId());
            if (cat != null) book.setCategoryName(cat.getName());
        }
        int total = bookCopyMapper.countByBookId(book.getId());
        int available = bookCopyMapper.countAvailableByBookId(book.getId());
        book.setTotalCopies(total);
        book.setAvailableCopies(available);
        // 最近30天内上架的图书标记为新书
        book.setIsNew(DateUtil.isWithinDefaultDays(book.getCreateTime()));
    }
}
