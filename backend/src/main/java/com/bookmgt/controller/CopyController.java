package com.bookmgt.controller;

import com.bookmgt.common.result.Result;
import com.bookmgt.dto.CopyRequest;
import com.bookmgt.entity.BookCopy;
import com.bookmgt.service.CopyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/copies")
public class CopyController {

    private final CopyService copyService;

    public CopyController(CopyService copyService) {
        this.copyService = copyService;
    }

    @GetMapping("/book/{bookId}")
    public Result<List<BookCopy>> listByBook(@PathVariable Long bookId) {
        return Result.success(copyService.getByBookId(bookId));
    }

    @PostMapping
    public Result<BookCopy> add(@RequestBody CopyRequest req) {
        return Result.success(copyService.addCopy(req));
    }

    @PostMapping("/batch")
    public Result<Void> batchAdd(@RequestBody List<CopyRequest> reqs) {
        copyService.batchAddCopies(reqs);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<BookCopy> update(@PathVariable Long id, @RequestBody CopyRequest req) {
        req.setId(id);
        return Result.success(copyService.updateCopy(req));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        copyService.deleteCopy(id);
        return Result.success();
    }
}
