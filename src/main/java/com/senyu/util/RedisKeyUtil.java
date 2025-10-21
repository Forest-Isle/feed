package com.senyu.util;

/**
 * Redis Key工具类
 *
 * @author senyu
 */
public class RedisKeyUtil {

    private static final String FEED_PREFIX = "feed:";
    private static final String USER_PREFIX = "user:";
    private static final String POST_PREFIX = "post:";
    private static final String FOLLOWER_PREFIX = "follower:";
    private static final String FOLLOWING_PREFIX = "following:";

    /**
     * 用户Feed流缓存Key
     * 使用ZSet存储，score为时间戳
     */
    public static String getUserFeedKey(Long userId) {
        return FEED_PREFIX + "timeline:" + userId;
    }

    /**
     * 用户发件箱缓存Key
     * 使用ZSet存储，score为时间戳
     */
    public static String getUserOutboxKey(Long userId) {
        return FEED_PREFIX + "outbox:" + userId;
    }

    /**
     * 用户信息缓存Key
     */
    public static String getUserInfoKey(Long userId) {
        return USER_PREFIX + "info:" + userId;
    }

    /**
     * 内容详情缓存Key
     */
    public static String getPostInfoKey(Long postId) {
        return POST_PREFIX + "info:" + postId;
    }

    /**
     * 用户粉丝列表Key
     * 使用Set存储
     */
    public static String getUserFollowersKey(Long userId) {
        return FOLLOWER_PREFIX + "list:" + userId;
    }

    /**
     * 用户关注列表Key
     * 使用Set存储
     */
    public static String getUserFollowingKey(Long userId) {
        return FOLLOWING_PREFIX + "list:" + userId;
    }

    /**
     * 热门内容缓存Key
     */
    public static String getHotPostsKey() {
        return POST_PREFIX + "hot";
    }
}
