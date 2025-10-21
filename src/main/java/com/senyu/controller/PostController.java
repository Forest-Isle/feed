package com.senyu.controller;

import com.senyu.annotation.Idempotent;
import com.senyu.annotation.RateLimit;
import com.senyu.common.Result;
import com.senyu.dto.PostPublishDTO;
import com.senyu.entity.Post;
import com.senyu.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * 内容接口
 *
 * @author senyu
 */
@Slf4j
@RestController
@RequestMapping("/post")
@Validated
@Tag(name = "内容管理", description = "内容发布、查询、点赞等操作")
public class PostController {

    @Resource
    private PostService postService;

    @Operation(summary = "发布内容", description = "发布新的图文/视频内容")
    @PostMapping("/publish")
    @RateLimit(time = 3600, count = 10, limitType = RateLimit.LimitType.USER)
    @Idempotent(prefix = "post:publish", expireTime = 300, message = "内容发布中，请勿重复提交")
    public Result<Long> publishPost(@RequestBody @Valid PostPublishDTO dto) {
        Post post = new Post();
        BeanUtils.copyProperties(dto, post);
        Long postId = postService.publishPost(post);
        return Result.success("发布成功", postId);
    }

    @Operation(summary = "获取内容详情", description = "根据内容ID获取详情")
    @GetMapping("/{postId}")
    @RateLimit(time = 60, count = 200, limitType = RateLimit.LimitType.USER)
    public Result<Post> getPost(
            @Parameter(description = "内容ID", required = true)
            @PathVariable @NotNull Long postId) {
        Post post = postService.getPostById(postId);
        if (post == null) {
            return Result.error("内容不存在");
        }
        return Result.success(post);
    }

    @Operation(summary = "点赞内容", description = "对指定内容点赞")
    @PostMapping("/like/{postId}")
    @RateLimit(time = 60, count = 50, limitType = RateLimit.LimitType.USER)
    @Idempotent(prefix = "post:like", expireTime = 60, message = "点赞操作过于频繁")
    public Result<Void> likePost(
            @Parameter(description = "内容ID", required = true)
            @PathVariable @NotNull Long postId) {
        postService.likePost(postId);
        return Result.success("点赞成功", null);
    }
}
