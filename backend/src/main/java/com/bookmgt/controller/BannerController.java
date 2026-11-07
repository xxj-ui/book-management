package com.bookmgt.controller;

import com.bookmgt.common.result.Result;
import com.bookmgt.entity.Banner;
import com.bookmgt.mapper.BannerMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/banners")
public class BannerController {

    private final BannerMapper bannerMapper;

    public BannerController(BannerMapper bannerMapper) { this.bannerMapper = bannerMapper; }

    @GetMapping
    public Result<List<Banner>> list() {
        return Result.success(bannerMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Banner>()
                        .eq(Banner::getStatus, 1).orderByAsc(Banner::getSortOrder)));
    }

    @GetMapping("/admin")
    public Result<List<Banner>> adminList() {
        return Result.success(bannerMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Banner>()
                        .orderByAsc(Banner::getSortOrder)));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Banner banner) { bannerMapper.insert(banner); return Result.success(); }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id); bannerMapper.updateById(banner); return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bannerMapper.update(null, new LambdaUpdateWrapper<Banner>()
                .eq(Banner::getId, id).set(Banner::getDeleted, 1));
        return Result.success();
    }
}
