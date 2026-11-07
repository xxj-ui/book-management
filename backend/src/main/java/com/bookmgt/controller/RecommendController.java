package com.bookmgt.controller;

import com.bookmgt.common.result.Result;
import com.bookmgt.entity.Recommend;
import com.bookmgt.mapper.RecommendMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommends")
public class RecommendController {

    private final RecommendMapper recommendMapper;

    public RecommendController(RecommendMapper recommendMapper) { this.recommendMapper = recommendMapper; }

    @GetMapping
    public Result<List<Recommend>> list() { return Result.success(recommendMapper.selectAllWithBook()); }

    @PostMapping
    public Result<Void> add(@RequestBody Recommend rec) { recommendMapper.insert(rec); return Result.success(); }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        recommendMapper.update(null, new LambdaUpdateWrapper<Recommend>()
                .eq(Recommend::getId, id).set(Recommend::getDeleted, 1));
        return Result.success();
    }

    @PutMapping("/sort")
    public Result<Void> updateSort(@RequestBody List<Recommend> list) {
        for (Recommend r : list) { recommendMapper.updateById(r); }
        return Result.success();
    }
}
