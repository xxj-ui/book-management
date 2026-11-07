package com.bookmgt.service.impl;

import com.bookmgt.util.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookmgt.common.constant.RoleConst;
import com.bookmgt.dto.BorrowRequest;
import com.bookmgt.dto.ExtendRequest;
import com.bookmgt.entity.*;
import com.bookmgt.mapper.*;
import com.bookmgt.service.BorrowService;
import com.bookmgt.service.HolidayService;
import com.bookmgt.service.NotificationService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRecordMapper borrowRecordMapper;
    private final BookCopyMapper bookCopyMapper;
    private final BookMapper bookMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final FineMapper fineMapper;
    private final BorrowConfigMapper borrowConfigMapper;
    private final HolidayService holidayService;
    private final NotificationService notificationService;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;

    public BorrowServiceImpl(BorrowRecordMapper borrowRecordMapper, BookCopyMapper bookCopyMapper,
                             BookMapper bookMapper, StudentMapper studentMapper,
                             TeacherMapper teacherMapper, FineMapper fineMapper,
                             BorrowConfigMapper borrowConfigMapper,
                             HolidayService holidayService,
                             NotificationService notificationService,
                             RedissonClient redissonClient,
                             StringRedisTemplate redisTemplate) {
        this.borrowRecordMapper = borrowRecordMapper;
        this.bookCopyMapper = bookCopyMapper;
        this.bookMapper = bookMapper;
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
        this.fineMapper = fineMapper;
        this.borrowConfigMapper = borrowConfigMapper;
        this.holidayService = holidayService;
        this.notificationService = notificationService;
        this.redissonClient = redissonClient;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Page<BorrowRecord> listRecords(int page, int size, String keyword, Integer status,
                                           LocalDate startDate, LocalDate endDate) {
        // 使用自定义分页查询
        List<BorrowRecord> all;
        if (status != null && status > 0) {
            all = borrowRecordMapper.selectByStatus(status);
        } else {
            all = borrowRecordMapper.selectAllWithDetail();
        }

        // 过滤
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            all = all.stream()
                    .filter(r -> (r.getUserName() != null && r.getUserName().toLowerCase().contains(kw))
                            || (r.getBookTitle() != null && r.getBookTitle().toLowerCase().contains(kw)))
                    .toList();
        }
        if (startDate != null) {
            all = all.stream().filter(r -> !r.getBorrowDate().isBefore(startDate)).toList();
        }
        if (endDate != null) {
            all = all.stream().filter(r -> !r.getBorrowDate().isAfter(endDate)).toList();
        }

        // 计算超期天数和滞纳金
        all.forEach(this::enrichRecord);

        // 手动分页
        int total = all.size();
        int from = (page - 1) * size;
        int to = Math.min(from + size, total);
        List<BorrowRecord> records = from >= total ? List.of() : all.subList(from, to);

        Page<BorrowRecord> result = new Page<>(page, size, total);
        result.setRecords(records);
        return result;
    }

    @Override
    public List<BorrowRecord> listByUserId(Long userId) {
        List<BorrowRecord> records = borrowRecordMapper.selectByUserId(userId);
        records.forEach(this::enrichRecord);
        return records;
    }

    @Override
    public List<BorrowRecord> listOverdue() {
        List<BorrowRecord> records = borrowRecordMapper.selectByStatus(RoleConst.BORROW_OVERDUE);
        records.forEach(this::enrichRecord);
        return records;
    }

    @Override
    public BorrowRecord getDetail(Long id) {
        BorrowRecord r = borrowRecordMapper.selectDetailById(id);
        if (r != null) enrichRecord(r);
        return r;
    }

    @Override
    @Transactional
    public BorrowRecord borrowBook(BorrowRequest req) {
        Long userId = req.getUserId();
        // Redisson 分布式锁，防止并发借阅
        RLock lock = redissonClient.getLock("borrow:user:" + userId);
        lock.lock();
        try {
            // ─── 1. 用户验证 ───
            Student student = studentMapper.selectById(userId);
            Teacher teacher = null;
            if (student == null) {
                teacher = teacherMapper.selectById(userId);
                if (teacher == null) throw new IllegalArgumentException("用户不存在");
                // 教师状态锁定检查
                if (teacher.getStatus() == 3) throw new IllegalArgumentException("该用户已被禁用");
            } else if (student.getStatus() == 3) {
                throw new IllegalArgumentException("该用户已被禁用");
            }
            // 超期锁定检查
            if (student != null && student.getStatus() == 2) {
                throw new IllegalArgumentException("该用户已被超期锁定，无法借书");
            }

            // ─── 2. 获取可借副本 ───
            BookCopy copy;
            if (req.getCopyId() != null) {
                copy = bookCopyMapper.selectById(req.getCopyId());
                if (copy == null || copy.getStatus() != 1)
                    throw new IllegalArgumentException("该副本不可借");
            } else if (req.getBookId() != null) {
                copy = bookCopyMapper.selectAvailableCopy(req.getBookId());
                if (copy == null) throw new IllegalArgumentException("该图书暂无在架可借的副本");
            } else {
                throw new IllegalArgumentException("请指定图书或副本");
            }

            // ─── 3. 检查借阅配额 ───
            Long dbUserId = student != null ? student.getId() : teacher.getId();
            Integer eduLevel = student != null ? student.getEducationLevel() : null;
            int role = student != null ? 1 : 2;
            BorrowConfig config = findBorrowConfig(role, eduLevel);

            int currentBorrows = borrowRecordMapper.countCurrentByUserId(dbUserId);
            if (currentBorrows >= config.getMaxBooks()) {
                throw new IllegalArgumentException("已达到最大借阅限额(" + config.getMaxBooks() + "册)");
            }

            // ─── 4. 新书限额检查（按 create_time 30天内） ───
            Book book = bookMapper.selectById(copy.getBookId());
            boolean isNew = book != null && DateUtil.isWithinDefaultDays(book.getCreateTime());
            System.out.println("[DEBUG] bookId=" + (book != null ? book.getId() : null)
                    + " createTime=" + (book != null ? book.getCreateTime() : null)
                    + " isNew=" + isNew
                    + " maxNewBooks=" + config.getMaxNewBooks());
            if (isNew && config.getMaxNewBooks() != null) {
                int currentNewBorrows = borrowRecordMapper.countCurrentNewBooksByUserId(dbUserId);
                System.out.println("[DEBUG] currentNewBorrows=" + currentNewBorrows);
                if (currentNewBorrows >= config.getMaxNewBooks()) {
                    throw new IllegalArgumentException("已达到新书借阅限额(" + config.getMaxNewBooks() + "册)");
                }
            }

            // ─── 5. 创建借阅记录 ───
            LocalDate borrowDate = LocalDate.now();
            LocalDate dueDate = borrowDate.plusDays(config.getMaxDays());
            try { dueDate = holidayService.adjustDueDate(dueDate); } catch (Exception ignored) {}

            BorrowRecord record = new BorrowRecord();
            String borrowNo = "B" + borrowDate.toString().replace("-", "")
                    + String.format("%04d", System.currentTimeMillis() % 10000);
            record.setBorrowNo(borrowNo);
            record.setUserId(dbUserId);
            record.setCopyId(copy.getId());
            record.setBorrowDate(borrowDate);
            record.setDueDate(dueDate);
            record.setStatus(RoleConst.BORROW_BORROWING);
            borrowRecordMapper.insert(record);

            // ─── 6. 更新副本状态 ───
            copy.setStatus(RoleConst.COPY_BORROWED);
            bookCopyMapper.updateById(copy);

            // 更新热门图书 ZSET
            try {
                redisTemplate.opsForZSet().incrementScore("hot_books",
                        String.valueOf(copy.getBookId()), 1);
            } catch (Exception ignored) {}

            return borrowRecordMapper.selectDetailById(record.getId());
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Transactional
    public BorrowRecord returnBook(Long recordId) {
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) throw new IllegalArgumentException("借阅记录不存在");
        if (record.getStatus() == RoleConst.BORROW_RETURNED || record.getStatus() == RoleConst.BORROW_RETURN_PENDING)
            throw new IllegalArgumentException("该图书已归还");

        LocalDate returnDate = LocalDate.now();
        record.setReturnDate(returnDate);
        record.setStatus(RoleConst.BORROW_RETURN_PENDING);
        borrowRecordMapper.updateById(record);

        // 更新副本状态为待上架（等待管理员确认上架）
        BookCopy copy = bookCopyMapper.selectById(record.getCopyId());
        if (copy != null) {
            copy.setStatus(RoleConst.COPY_PENDING);
            bookCopyMapper.updateById(copy);
        }

        return borrowRecordMapper.selectDetailById(recordId);
    }

    @Override
    @Transactional
    public BorrowRecord renewBook(Long recordId) {
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) throw new IllegalArgumentException("借阅记录不存在");
        if (record.getStatus() != RoleConst.BORROW_BORROWING)
            throw new IllegalArgumentException("只能续借借阅中的图书");

        // 延期30天
        LocalDate newDueDate = record.getDueDate().plusDays(30);
        record.setDueDate(newDueDate);
        borrowRecordMapper.updateById(record);
        return borrowRecordMapper.selectDetailById(recordId);
    }

    @Override
    @Transactional
    public BorrowRecord extendDueDate(ExtendRequest req) {
        BorrowRecord record = borrowRecordMapper.selectById(req.getRecordId());
        if (record == null) throw new IllegalArgumentException("借阅记录不存在");
        LocalDate oldDue = record.getDueDate();
        record.setDueDate(req.getNewDueDate());
        borrowRecordMapper.updateById(record);

        // 发送延期通知
        try {
            BorrowRecord detail = borrowRecordMapper.selectDetailById(req.getRecordId());
            if (detail != null) {
                String title = "📅 延期通知";
                String content = "您借阅的《" + (detail.getBookTitle() != null ? detail.getBookTitle() : "图书")
                        + "》应还日期已从 " + oldDue + " 延至 " + req.getNewDueDate() + "。";
                notificationService.addNotification(detail.getUserId(), 1, title, content);
            }
        } catch (Exception ignored) {}

        return borrowRecordMapper.selectDetailById(req.getRecordId());
    }

    @Override
    public void batchRemind(List<Long> recordIds) {
        for (Long id : recordIds) {
            BorrowRecord r = borrowRecordMapper.selectDetailById(id);
            if (r != null) {
                String title = "⏰ 超期催还通知";
                String content = "您借阅的《" + (r.getBookTitle() != null ? r.getBookTitle() : "图书")
                        + "》已于 " + r.getDueDate() + " 到期，请尽快归还。";
                try {
                    notificationService.addNotification(r.getUserId(), 2, title, content);
                } catch (Exception ignored) {}
            }
        }
    }

    private void enrichRecord(BorrowRecord r) {
        if (r.getStatus() == RoleConst.BORROW_OVERDUE || r.getReturnDate() == null) {
            LocalDate now = LocalDate.now();
            if (r.getDueDate() != null && now.isAfter(r.getDueDate())) {
                long days = ChronoUnit.DAYS.between(r.getDueDate(), now);
                r.setOverdueDays((int) days);
                // 根据用户角色查询实际滞纳金费率
                BigDecimal rate = BigDecimal.valueOf(0.10);
                try {
                    Student s = studentMapper.selectById(r.getUserId());
                    if (s != null) {
                        BorrowConfig cfg = findBorrowConfig(1, s.getEducationLevel());
                        if (cfg != null) rate = cfg.getFinePerDay();
                    } else {
                        Teacher t = teacherMapper.selectById(r.getUserId());
                        if (t != null) {
                            BorrowConfig cfg = findBorrowConfig(2, null);
                            if (cfg != null) rate = cfg.getFinePerDay();
                        }
                    }
                } catch (Exception ignored) {}
                r.setFineAmount(days * rate.doubleValue());
            }
        }

        // 查询借阅次数
        List<Fine> fines = fineMapper.selectList(
                new LambdaQueryWrapper<Fine>().eq(Fine::getRecordId, r.getId()));
        if (fines != null && !fines.isEmpty()) {
            r.setFineAmount(fines.get(0).getFineAmount().doubleValue());
        }
    }

    private BorrowConfig findBorrowConfig(int role, Integer educationLevel) {
        String cacheKey = "cache:borrow_config:" + role + ":" + (educationLevel != null ? educationLevel : "default");
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return new com.fasterxml.jackson.databind.ObjectMapper().readValue(cached, BorrowConfig.class);
            } catch (Exception ignored) {}
        }

        LambdaQueryWrapper<BorrowConfig> qw = new LambdaQueryWrapper<BorrowConfig>()
                .eq(BorrowConfig::getRole, role);
        if (educationLevel != null) {
            qw.eq(BorrowConfig::getEducationLevel, educationLevel);
        }
        BorrowConfig config = borrowConfigMapper.selectOne(qw);
        if (config == null) {
            config = borrowConfigMapper.selectOne(
                    new LambdaQueryWrapper<BorrowConfig>().eq(BorrowConfig::getRole, role).isNull(BorrowConfig::getEducationLevel));
        }
        if (config == null) {
            config = new BorrowConfig();
            config.setMaxBooks(5);
            config.setMaxDays(30);
            config.setFinePerDay(BigDecimal.valueOf(0.10));
            config.setMaxNewBooks(2);
        }

        try {
            redisTemplate.opsForValue().set(cacheKey,
                    new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(config),
                    2, java.util.concurrent.TimeUnit.HOURS);
        } catch (Exception ignored) {}

        return config;
    }
}
