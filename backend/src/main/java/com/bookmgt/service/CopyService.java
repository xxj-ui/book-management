package com.bookmgt.service;

import com.bookmgt.entity.BookCopy;
import com.bookmgt.dto.CopyRequest;

import java.util.List;

public interface CopyService {
    List<BookCopy> getByBookId(Long bookId);
    BookCopy addCopy(CopyRequest req);
    BookCopy updateCopy(CopyRequest req);
    void deleteCopy(Long id);
    void batchAddCopies(List<CopyRequest> reqs);
}
