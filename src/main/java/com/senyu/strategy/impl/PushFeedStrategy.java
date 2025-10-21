package com.senyu.strategy.impl;

import cn.hutool.core.collection.CollUtil;
import com.senyu.config.FeedConfig;
import com.senyu.entity.FeedInbox;
import com.senyu.entity.Post;
import com.senyu.mapper.FeedInboxMapper;
import com.senyu.mapper.FollowMapper;
import com.senyu.strategy.FeedStrategy;
import com.senyu.util.RedisKeyUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 推模式Feed流策略
 * 适用于粉丝数较少的用户，发布内容时主动推送到所有粉丝的收件箱
 *
 * @author senyu
 */
@Slf4j
@Component("pushFeedStrategy")
public class PushFeedStrategy implements FeedStrategy {

    @Resource
    private FollowMapper followMapper;

    @Resource
    private FeedInboxMapper feedInboxMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private FeedConfig feedConfig;

    @Override
    public void dispatchFeed(Post post) {
        log.info("使用推模式分发Feed，内容ID：{}, 作者ID：{}", post.getId(), post.getUserId());

        // 1. 获取活跃粉丝列表
        List<Long> followerIds = followMapper.selectActiveFollowerIds(post.getUserId());

        if (CollUtil.isEmpty(followerIds)) {
            log.info("用户{}没有活跃粉丝，无需推送", post.getUserId());
            return;
        }

        log.info("用户{}有{}个活跃粉丝，开始推送", post.getUserId(), followerIds.size());

        // 2. 批量插入到粉丝的Feed收件箱（数据库）
        List<FeedInbox> feedInboxList = followerIds.stream()
                .map(followerId -> {
                    FeedInbox feedInbox = new FeedInbox();
                    feedInbox.setUserId(followerId);
                    feedInbox.setPostId(post.getId());
                    feedInbox.setAuthorId(post.getUserId());
                    feedInbox.setCreatedAt(LocalDateTime.now());
                    return feedInbox;
                })
                .collect(Collectors.toList());

        // 分批插入，避免单次插入过多
        int batchSize = 1000;
        for (int i = 0; i < feedInboxList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, feedInboxList.size());
            List<FeedInbox> batch = feedInboxList.subList(i, end);
            feedInboxMapper.insertBatch(batch);
        }

        // 3. 推送到Redis缓存（异步）
        followerIds.forEach(followerId -> {
            String feedKey = RedisKeyUtil.getUserFeedKey(followerId);
            try {
                // 使用ZSet存储，score为时间戳
                redisTemplate.opsForZSet().add(
                        feedKey,
                        post.getId(),
                        System.currentTimeMillis()
                );

                // 保持Feed流长度限制
                Long size = redisTemplate.opsForZSet().size(feedKey);
                if (size != null && size > feedConfig.getMaxFeedSize()) {
                    redisTemplate.opsForZSet().removeRange(feedKey, 0, size - feedConfig.getMaxFeedSize() - 1);
                }

                // 设置过期时间
                redisTemplate.expire(feedKey, feedConfig.getCacheTtl(), TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("推送到Redis失败，用户ID：{}, 内容ID：{}", followerId, post.getId(), e);
            }
        });

        log.info("推模式分发完成，共推送给{}个粉丝", followerIds.size());
    }

    @Override
    public String getStrategyName() {
        return "PUSH";
    }
}
