package com.senyu.service;

import cn.hutool.core.collection.CollUtil;
import com.senyu.common.PageResult;
import com.senyu.config.FeedConfig;
import com.senyu.entity.FeedInbox;
import com.senyu.entity.Post;
import com.senyu.mapper.FeedInboxMapper;
import com.senyu.mapper.PostMapper;
import com.senyu.util.RedisKeyUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Feed流服务
 *
 * @author senyu
 */
@Slf4j
@Service
public class FeedService {

    @Resource
    private FeedInboxMapper feedInboxMapper;

    @Resource
    private PostMapper postMapper;

    @Resource
    private FollowService followService;

    @Resource
    private PostService postService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private FeedConfig feedConfig;

    /**
     * 获取用户Feed流（推模式）
     * 从用户的收件箱获取Feed
     */
    public PageResult<Post> getUserFeed(Long userId, Long maxId, Integer pageSize) {
        log.info("获取用户Feed流，用户ID：{}，maxId：{}", userId, maxId);

        if (pageSize == null || pageSize <= 0) {
            pageSize = feedConfig.getPageSize();
        }

        List<Long> postIds = new ArrayList<>();

        // 1. 先尝试从Redis缓存获取
        String feedKey = RedisKeyUtil.getUserFeedKey(userId);
        Long cacheSize = redisTemplate.opsForZSet().size(feedKey);

        if (cacheSize != null && cacheSize > 0) {
            log.info("从Redis缓存获取Feed流");
            postIds = getPostIdsFromCache(feedKey, maxId, pageSize);
        }

        // 2. 缓存未命中或数据不足，从数据库获取
        if (postIds.isEmpty()) {
            log.info("缓存未命中，从数据库获取Feed流");
            postIds = feedInboxMapper.selectUserFeed(userId, maxId, pageSize)
                    .stream()
                    .map(FeedInbox::getPostId)
                    .collect(Collectors.toList());
        }

        // 3. 如果仍然为空，尝试使用拉模式补充
        if (postIds.isEmpty()) {
            log.info("收件箱为空，使用拉模式获取关注人的最新内容");
            postIds = pullFeedFromFollowing(userId, pageSize);
        }

        // 4. 批量查询内容详情
        List<Post> posts = postService.getPostsByIds(postIds);

        // 5. 确定是否有下一页和下一个游标
        boolean hasNext = posts.size() >= pageSize;
        Long nextCursor = hasNext && !posts.isEmpty() ?
                posts.get(posts.size() - 1).getId() : null;

        return new PageResult<>(posts, nextCursor, hasNext);
    }

    /**
     * 获取推荐Feed流（基于热门内容）
     */
    public PageResult<Post> getRecommendFeed(Long userId, Integer page, Integer pageSize) {
        log.info("获取推荐Feed流，用户ID：{}", userId);

        if (page == null || page <= 0) {
            page = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = feedConfig.getPageSize();
        }

        // TODO: 实现推荐算法，这里简单返回最新的热门内容
        // 可以基于：1. 用户兴趣标签 2. 协同过滤 3. 热度排序等

        List<Long> postIds = new ArrayList<>();

        // 1. 先从热门内容缓存获取
        String hotPostsKey = RedisKeyUtil.getHotPostsKey();
        Set<Object> hotPostIds = redisTemplate.opsForZSet()
                .reverseRange(hotPostsKey, (long) (page - 1) * pageSize, (long) page * pageSize - 1);

        if (CollUtil.isNotEmpty(hotPostIds)) {
            postIds = hotPostIds.stream()
                    .map(obj -> Long.parseLong(obj.toString()))
                    .collect(Collectors.toList());
        }

        // 2. 缓存未命中，从数据库获取热门内容
        if (postIds.isEmpty()) {
            log.info("热门缓存未命中，从数据库获取");
            // 简单按点赞数排序
            List<Post> hotPosts = postMapper.selectList(null);
            hotPosts.sort((p1, p2) -> p2.getLikeCount().compareTo(p1.getLikeCount()));

            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, hotPosts.size());

            if (start < hotPosts.size()) {
                return new PageResult<>(
                        hotPosts.subList(start, end),
                        (long) hotPosts.size(),
                        page,
                        pageSize
                );
            }
        }

        // 3. 批量查询内容详情
        List<Post> posts = postService.getPostsByIds(postIds);

        return new PageResult<>(posts, (long) posts.size(), page, pageSize);
    }

    /**
     * 从Redis缓存获取Post ID列表
     */
    private List<Long> getPostIdsFromCache(String feedKey, Long maxId, int limit) {
        Set<ZSetOperations.TypedTuple<Object>> tuples;

        if (maxId != null) {
            // 使用score范围查询（score是时间戳）
            tuples = redisTemplate.opsForZSet()
                    .reverseRangeByScoreWithScores(feedKey, 0, maxId, 0, limit);
        } else {
            // 获取最新的
            tuples = redisTemplate.opsForZSet()
                    .reverseRangeWithScores(feedKey, 0, limit - 1);
        }

        if (CollUtil.isEmpty(tuples)) {
            return new ArrayList<>();
        }

        return tuples.stream()
                .map(tuple -> Long.parseLong(tuple.getValue().toString()))
                .collect(Collectors.toList());
    }

    /**
     * 拉模式：从关注的人的发件箱拉取内容
     */
    private List<Long> pullFeedFromFollowing(Long userId, int limit) {
        // 1. 获取关注列表
        List<Long> followingIds = followService.getFollowingIds(userId);

        if (followingIds.isEmpty()) {
            log.info("用户{}未关注任何人", userId);
            return new ArrayList<>();
        }

        log.info("用户{}关注了{}个人，开始拉取他们的最新内容", userId, followingIds.size());

        // 2. 从关注的用户的发件箱获取最新内容
        List<Post> posts = postMapper.selectLatestByUserIds(followingIds, limit);

        return posts.stream()
                .map(Post::getId)
                .collect(Collectors.toList());
    }

    /**
     * 刷新用户Feed缓存
     */
    public void refreshUserFeedCache(Long userId) {
        log.info("刷新用户Feed缓存，用户ID：{}", userId);

        String feedKey = RedisKeyUtil.getUserFeedKey(userId);

        // 清空旧缓存
        redisTemplate.delete(feedKey);

        // 从数据库重新加载
        List<Long> postIds = feedInboxMapper.selectUserFeed(userId, null, feedConfig.getMaxFeedSize())
                .stream()
                .map(FeedInbox::getPostId)
                .toList();

        if (!postIds.isEmpty()) {
            // 重新写入缓存
            postIds.forEach(postId -> {
                redisTemplate.opsForZSet().add(feedKey, postId, System.currentTimeMillis());
            });
        }

        log.info("Feed缓存刷新完成，共加载{}条内容", postIds.size());
    }
}
