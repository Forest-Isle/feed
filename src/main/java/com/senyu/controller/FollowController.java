package com.senyu.controller;

import com.senyu.common.Result;
import com.senyu.service.FollowService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 关注接口
 *
 * @author senyu
 */
@Slf4j
@RestController
@RequestMapping("/follow")
@Validated
public class FollowController {

    @Resource
    private FollowService followService;

    /**
     * 关注用户
     */
    @PostMapping("/{followeeId}")
    public Result<Void> follow(
            @RequestHeader("userId") @NotNull Long userId,
            @PathVariable @NotNull Long followeeId) {
        try {
            followService.follow(userId, followeeId);
            return Result.success("关注成功", null);
        } catch (Exception e) {
            log.error("关注失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消关注
     */
    @DeleteMapping("/{followeeId}")
    public Result<Void> unfollow(
            @RequestHeader("userId") @NotNull Long userId,
            @PathVariable @NotNull Long followeeId) {
        try {
            followService.unfollow(userId, followeeId);
            return Result.success("取消关注成功", null);
        } catch (Exception e) {
            log.error("取消关注失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查是否已关注
     */
    @GetMapping("/check/{followeeId}")
    public Result<Boolean> isFollowing(
            @RequestHeader("userId") @NotNull Long userId,
            @PathVariable @NotNull Long followeeId) {
        try {
            boolean isFollowing = followService.isFollowing(userId, followeeId);
            return Result.success(isFollowing);
        } catch (Exception e) {
            log.error("检查关注状态失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取粉丝列表
     */
    @GetMapping("/followers/{userId}")
    public Result<List<Long>> getFollowers(@PathVariable @NotNull Long userId) {
        try {
            List<Long> followers = followService.getFollowerIds(userId);
            return Result.success(followers);
        } catch (Exception e) {
            log.error("获取粉丝列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取关注列表
     */
    @GetMapping("/following/{userId}")
    public Result<List<Long>> getFollowing(@PathVariable @NotNull Long userId) {
        try {
            List<Long> following = followService.getFollowingIds(userId);
            return Result.success(following);
        } catch (Exception e) {
            log.error("获取关注列表失败", e);
            return Result.error(e.getMessage());
        }
    }
}
