package com.senyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.senyu.entity.Post;
import com.senyu.mapper.PostMapper;
import com.senyu.mapper.UserMapper;
import com.senyu.strategy.impl.HybridFeedStrategy;
import com.senyu.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 内容服务
 *
 * @author senyu
 */
@Slf4j
@Service
public class PostService {

    @Resource
    private PostMapper postMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private HybridFeedStrategy hybridFeedStrategy;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 发布内容
     */
    @Transactional(rollbackFor = Exception.class)
    public Long publishPost(Post post) {
        log.info("开始发布内容，用户ID：{}", post.getUserId());

        // 1. 保存内容到数据库
        post.setStatus(1); // 已发布
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setShareCount(0);
        post.setViewCount(0);
        postMapper.insert(post);

        // 2. 更新用户发帖数
        userMapper.increasePostCount(post.getUserId());

        // 3. 使用混合策略分发Feed流
        try {
            hybridFeedStrategy.dispatchFeed(post);
        } catch (Exception e) {
            log.error("分发Feed流失败，内容ID：{}", post.getId(), e);
            // 不影响主流程，异步处理可以容忍失败
        }

        // 4. 缓存内容详情
        cachePostInfo(post);

        log.info("内容发布成功，内容ID：{}", post.getId());
        return post.getId();
    }

    /**
     * 获取内容详情
     */
    public Post getPostById(Long postId) {
        // 先从缓存获取
        String cacheKey = RedisKeyUtil.getPostInfoKey(postId);
        Post post = (Post) redisTemplate.opsForValue().get(cacheKey);

        if (post != null) {
            log.info("从缓存获取内容详情，内容ID：{}", postId);
            // 异步增加浏览数
            postMapper.increaseViewCount(postId);
            return post;
        }

        // 缓存未命中，从数据库查询
        post = postMapper.selectById(postId);
        if (post != null) {
            cachePostInfo(post);
            postMapper.increaseViewCount(postId);
        }

        return post;
    }

    /**
     * 根据ID列表批量获取内容
     */
    public List<Post> getPostsByIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Post::getId, postIds)
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getCreatedAt);

        return postMapper.selectList(wrapper);
    }

    /**
     * 点赞内容
     */
    @Transactional(rollbackFor = Exception.class)
    public void likePost(Long postId) {
        postMapper.increaseLikeCount(postId, 1);
        // 清除缓存
        redisTemplate.delete(RedisKeyUtil.getPostInfoKey(postId));
    }

    /**
     * 缓存内容详情
     */
    private void cachePostInfo(Post post) {
        String cacheKey = RedisKeyUtil.getPostInfoKey(post.getId());
        redisTemplate.opsForValue().set(cacheKey, post, 3600, TimeUnit.SECONDS);
    }
}
