package com.senyu.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具
 *
 * @author senyu
 */
@Slf4j
@Component
public class RedisLockUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String LOCK_PREFIX = "lock:";

    // Lua脚本保证释放锁的原子性
    private static final String UNLOCK_LUA_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    /**
     * 尝试获取锁
     *
     * @param key        锁的key
     * @param expireTime 过期时间（秒）
     * @return 锁的唯一标识，获取失败返回null
     */
    public String tryLock(String key, long expireTime) {
        String lockKey = LOCK_PREFIX + key;
        String requestId = UUID.randomUUID().toString();

        try {
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, requestId, expireTime, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(success)) {
                log.debug("获取锁成功，key: {}, requestId: {}", lockKey, requestId);
                return requestId;
            }
        } catch (Exception e) {
            log.error("获取锁异常，key: {}", lockKey, e);
        }

        return null;
    }

    /**
     * 释放锁
     *
     * @param key       锁的key
     * @param requestId 锁的唯一标识
     * @return 是否释放成功
     */
    public boolean unlock(String key, String requestId) {
        String lockKey = LOCK_PREFIX + key;

        try {
            Long result = redisTemplate.execute(
                    RedisScript.of(UNLOCK_LUA_SCRIPT, Long.class),
                    Collections.singletonList(lockKey),
                    requestId
            );

            boolean success = result != null && result == 1L;
            if (success) {
                log.debug("释放锁成功，key: {}, requestId: {}", lockKey, requestId);
            } else {
                log.warn("释放锁失败，锁已过期或不属于当前请求，key: {}, requestId: {}", lockKey, requestId);
            }
            return success;

        } catch (Exception e) {
            log.error("释放锁异常，key: {}, requestId: {}", lockKey, requestId, e);
            return false;
        }
    }

    /**
     * 执行带锁的操作
     *
     * @param key        锁的key
     * @param expireTime 过期时间（秒）
     * @param task       要执行的任务
     * @param <T>        返回值类型
     * @return 任务执行结果
     */
    public <T> T executeWithLock(String key, long expireTime, LockTask<T> task) {
        String requestId = tryLock(key, expireTime);
        if (requestId == null) {
            throw new RuntimeException("获取锁失败，请稍后重试");
        }

        try {
            return task.execute();
        } finally {
            unlock(key, requestId);
        }
    }

    @FunctionalInterface
    public interface LockTask<T> {
        T execute();
    }
}
