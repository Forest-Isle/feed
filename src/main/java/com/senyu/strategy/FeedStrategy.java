package com.senyu.strategy;

import com.senyu.entity.Post;

/**
 * Feed流策略接口
 *
 * @author senyu
 */
public interface FeedStrategy {

    /**
     * 分发内容到Feed流
     *
     * @param post 发布的内容
     */
    void dispatchFeed(Post post);

    /**
     * 策略名称
     */
    String getStrategyName();
}
