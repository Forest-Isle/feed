package com.senyu.strategy.impl;

import com.senyu.config.FeedConfig;
import com.senyu.entity.Post;
import com.senyu.entity.User;
import com.senyu.mapper.UserMapper;
import com.senyu.strategy.FeedStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * 混合模式Feed流策略
 * 根据用户粉丝数动态选择推模式或拉模式
 *
 * @author senyu
 */
@Slf4j
@Component("hybridFeedStrategy")
public class HybridFeedStrategy implements FeedStrategy {

    @Resource
    private UserMapper userMapper;

    @Resource
    private FeedConfig feedConfig;

    @Resource
    private PushFeedStrategy pushFeedStrategy;

    @Resource
    private PullFeedStrategy pullFeedStrategy;

    @Override
    public void dispatchFeed(Post post) {
        log.info("使用混合模式分发Feed，内容ID：{}, 作者ID：{}", post.getId(), post.getUserId());

        // 1. 查询作者信息
        User author = userMapper.selectById(post.getUserId());
        if (author == null) {
            log.error("作者不存在，用户ID：{}", post.getUserId());
            return;
        }

        Integer followerCount = author.getFollowerCount();
        log.info("作者粉丝数：{}", followerCount);

        // 2. 根据粉丝数选择策略
        if (followerCount <= feedConfig.getPushFanThreshold()) {
            // 粉丝数较少，使用推模式
            log.info("粉丝数 {} <= 推模式阈值 {}，使用推模式", followerCount, feedConfig.getPushFanThreshold());
            pushFeedStrategy.dispatchFeed(post);
        } else if (followerCount >= feedConfig.getPullFanThreshold()) {
            // 粉丝数非常多，使用拉模式
            log.info("粉丝数 {} >= 拉模式阈值 {}，使用拉模式", followerCount, feedConfig.getPullFanThreshold());
            pullFeedStrategy.dispatchFeed(post);
        } else {
            // 中等粉丝数，采用混合策略：推给活跃粉丝 + 存入发件箱
            log.info("粉丝数 {} 处于中间区间，使用混合策略", followerCount);
            pushFeedStrategy.dispatchFeed(post);  // 推给活跃粉丝
            pullFeedStrategy.dispatchFeed(post);   // 同时存入发件箱
        }
    }

    @Override
    public String getStrategyName() {
        return "HYBRID";
    }
}
