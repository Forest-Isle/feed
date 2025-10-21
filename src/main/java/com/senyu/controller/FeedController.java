package com.senyu.controller;

import com.senyu.annotation.RateLimit;
import com.senyu.common.PageResult;
import com.senyu.common.Result;
import com.senyu.entity.Post;
import com.senyu.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * Feed流接口
 *
 * @author senyu
 */
@Slf4j
@RestController
@RequestMapping("/feed")
@Validated
@Tag(name = "Feed流管理", description = "Feed流相关接口，包括时间线、推荐流等")
public class FeedController {

    @Resource
    private FeedService feedService;

    @Operation(summary = "获取关注Feed流", description = "获取用户关注的Feed时间线，支持滚动加载")
    @GetMapping("/timeline")
    @RateLimit(time = 60, count = 100, limitType = RateLimit.LimitType.USER)
    public Result<PageResult<Post>> getTimeline(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("userId") @NotNull Long userId,
            @Parameter(description = "游标ID，用于分页，首次请求不传")
            @RequestParam(required = false) Long maxId,
            @Parameter(description = "每页大小，默认20")
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        PageResult<Post> result = feedService.getUserFeed(userId, maxId, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "获取推荐Feed流", description = "获取基于热门内容的推荐Feed")
    @GetMapping("/recommend")
    @RateLimit(time = 60, count = 100, limitType = RateLimit.LimitType.USER)
    public Result<PageResult<Post>> getRecommend(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("userId") @NotNull Long userId,
            @Parameter(description = "页码，默认1")
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页大小，默认20")
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        PageResult<Post> result = feedService.getRecommendFeed(userId, page, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "刷新Feed缓存", description = "手动刷新用户的Feed缓存")
    @PostMapping("/refresh")
    @RateLimit(time = 300, count = 5, limitType = RateLimit.LimitType.USER)
    public Result<Void> refreshFeed(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("userId") @NotNull Long userId) {
        feedService.refreshUserFeedCache(userId);
        return Result.success("刷新成功", null);
    }
}
