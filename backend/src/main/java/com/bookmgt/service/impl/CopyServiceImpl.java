package com.bookmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bookmgt.dto.CopyRequest;
import com.bookmgt.entity.BookCopy;
import com.bookmgt.mapper.BookCopyMapper;
import com.bookmgt.service.CopyService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CopyServiceImpl implements CopyService {

    private final BookCopyMapper bookCopyMapper;

    public CopyServiceImpl(BookCopyMapper bookCopyMapper) {
        this.bookCopyMapper = bookCopyMapper;
    }

    @Override
    public List<BookCopy> getByBookId(Long bookId) {
        return bookCopyMapper.selectByBookIdWithBorrower(bookId);
    }

    @Override
    public BookCopy addCopy(CopyRequest req) {
        if (req.getBarcode() == null || req.getBarcode().isBlank()) {
            throw new IllegalArgumentException("条码号不能为空");
        }
        // 检查条码是否已存在
        BookCopy exist = bookCopyMapper.selectOne(
                new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getBarcode, req.getBarcode()));
        if (exist != null) throw new IllegalArgumentException("条码号 " + req.getBarcode() + " 已存在");

        BookCopy copy = new BookCopy();
        copy.setBookId(req.getBookId());
        copy.setBarcode(req.getBarcode());
        copy.setShelfFloor(req.getShelfFloor());
        copy.setShelfZone(req.getShelfZone());
        copy.setShelfNo(req.getShelfNo());
        copy.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        copy.setPurchaseDate(req.getPurchaseDate() != null ? req.getPurchaseDate() : LocalDate.now());
        bookCopyMapper.insert(copy);
        return copy;
    }

    @Override
    public BookCopy updateCopy(CopyRequest req) {
        BookCopy copy = bookCopyMapper.selectById(req.getId());
        if (copy == null) throw new IllegalArgumentException("副本不存在");
        if (req.getBarcode() != null) {
            if (req.getBarcode().isBlank()) throw new IllegalArgumentException("条码号不能为空");
            if (!req.getBarcode().equals(copy.getBarcode())) {
                BookCopy exist = bookCopyMapper.selectOne(
                        new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getBarcode, req.getBarcode()));
                if (exist != null) throw new IllegalArgumentException("条码号 " + req.getBarcode() + " 已存在");
            }
            copy.setBarcode(req.getBarcode());
        }
        if (req.getShelfFloor() != null) copy.setShelfFloor(req.getShelfFloor());
        if (req.getShelfZone() != null) copy.setShelfZone(req.getShelfZone());
        if (req.getShelfNo() != null) copy.setShelfNo(req.getShelfNo());
        if (req.getStatus() != null) copy.setStatus(req.getStatus());
        if (req.getPurchaseDate() != null) copy.setPurchaseDate(req.getPurchaseDate());
        bookCopyMapper.updateById(copy);
        return copy;
    }

    @Override
    public void deleteCopy(Long id) {
        bookCopyMapper.update(null, new LambdaUpdateWrapper<BookCopy>()
                .eq(BookCopy::getId, id)
                .set(BookCopy::getDeleted, 1));
    }

    @Override
    public void batchAddCopies(List<CopyRequest> reqs) {
        for (CopyRequest req : reqs) {
            addCopy(req);
        }
    }
}
