package com.senyu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Feed流配置
 *
 * @author senyu
 */
@Data
@Component
@ConfigurationProperties(prefix = "feed")
public class FeedConfig {

    /**
     * 推模式粉丝数阈值（小于等于此值使用推模式）
     */
    private Integer pushFanThreshold = 1000;

    /**
     * 拉模式粉丝数阈值（大于等于此值使用拉模式）
     */
    private Integer pullFanThreshold = 10000;

    /**
     * Feed流缓存时长（秒）
     */
    private Long cacheTtl = 86400L;

    /**
     * Feed流最大长度
     */
    private Integer maxFeedSize = 1000;

    /**
     * 单次拉取Feed数量
     */
    private Integer pageSize = 20;
}
