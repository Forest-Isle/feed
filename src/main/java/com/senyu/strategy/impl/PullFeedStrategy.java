package com.senyu.strategy.impl;

import com.senyu.config.FeedConfig;
import com.senyu.entity.FeedOutbox;
import com.senyu.entity.Post;
import com.senyu.mapper.FeedOutboxMapper;
import com.senyu.strategy.FeedStrategy;
import com.senyu.util.RedisKeyUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 拉模式Feed流策略
 * 适用于粉丝数非常多的大V，内容存入发件箱，由粉丝主动拉取
 *
 * @author senyu
 */
@Slf4j
@Component("pullFeedStrategy")
public class PullFeedStrategy implements FeedStrategy {

    @Resource
    private FeedOutboxMapper feedOutboxMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private FeedConfig feedConfig;

    @Override
    public void dispatchFeed(Post post) {
        log.info("使用拉模式分发Feed，内容ID：{}, 作者ID：{}", post.getId(), post.getUserId());

        // 1. 插入到作者的发件箱（数据库）
        FeedOutbox feedOutbox = new FeedOutbox();
        feedOutbox.setUserId(post.getUserId());
        feedOutbox.setPostId(post.getId());
        feedOutbox.setCreatedAt(LocalDateTime.now());
        feedOutboxMapper.insert(feedOutbox);

        // 2. 存入Redis缓存
        String outboxKey = RedisKeyUtil.getUserOutboxKey(post.getUserId());
        try {
            // 使用ZSet存储，score为时间戳
            redisTemplate.opsForZSet().add(
                    outboxKey,
                    post.getId(),
                    System.currentTimeMillis()
            );

            // 保持发件箱长度限制
            Long size = redisTemplate.opsForZSet().size(outboxKey);
            if (size != null && size > feedConfig.getMaxFeedSize()) {
                redisTemplate.opsForZSet().removeRange(outboxKey, 0, size - feedConfig.getMaxFeedSize() - 1);
            }

            // 设置过期时间
            redisTemplate.expire(outboxKey, feedConfig.getCacheTtl(), TimeUnit.SECONDS);

            log.info("拉模式分发完成，内容已存入发件箱");
        } catch (Exception e) {
            log.error("存入Redis发件箱失败，用户ID：{}, 内容ID：{}", post.getUserId(), post.getId(), e);
        }
    }

    @Override
    public String getStrategyName() {
        return "PULL";
    }
}
