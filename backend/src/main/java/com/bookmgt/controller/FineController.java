package com.bookmgt.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.bookmgt.common.result.Result;
import com.bookmgt.entity.Fine;
import com.bookmgt.mapper.FineMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fines")
public class FineController {

    private final FineMapper fineMapper;

    public FineController(FineMapper fineMapper) {
        this.fineMapper = fineMapper;
    }

    /** 获取当前用户未支付的滞纳金 */
    @GetMapping
    public Result<List<Fine>> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(fineMapper.selectUnpaidByUserId(userId));
    }

    /** 支付滞纳金 → 已支付 + 软删除 */
    @PutMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        fineMapper.update(null, new LambdaUpdateWrapper<Fine>()
                .eq(Fine::getId, id)
                .set(Fine::getStatus, 2)
                .set(Fine::getDeleted, 1));
        return Result.successMsg("缴费成功");
    }
}
