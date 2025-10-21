package com.senyu.interceptor;

import cn.hutool.core.util.StrUtil;
import com.senyu.annotation.RateLimit;
import com.senyu.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 限流拦截器
 *
 * @author senyu
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // Lua脚本实现原子性限流
    private static final String LUA_SCRIPT =
            "local key = KEYS[1]\n" +
            "local limit = tonumber(ARGV[1])\n" +
            "local expire = tonumber(ARGV[2])\n" +
            "local current = tonumber(redis.call('get', key) or '0')\n" +
            "if current + 1 > limit then\n" +
            "    return 0\n" +
            "else\n" +
            "    redis.call('INCRBY', key, 1)\n" +
            "    if current == 0 then\n" +
            "        redis.call('EXPIRE', key, expire)\n" +
            "    end\n" +
            "    return current + 1\n" +
            "end";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        if (rateLimit == null) {
            return true;
        }

        String key = generateKey(request, rateLimit);
        long count = rateLimit.count();
        long time = rateLimit.time();

        try {
            Long result = redisTemplate.execute(
                    RedisScript.of(LUA_SCRIPT, Long.class),
                    Collections.singletonList(key),
                    count,
                    time
            );

            if (result == null || result == 0) {
                log.warn("请求被限流，key: {}, limit: {}/{} seconds", key, count, time);
                throw new BusinessException(429, "请求过于频繁，请稍后再试");
            }

            log.debug("限流检查通过，key: {}, 当前: {}, 限制: {}/{} seconds", key, result, count, time);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("限流检查异常", e);
            // 限流异常时放行，保证可用性
            return true;
        }
    }

    /**
     * 生成限流key
     */
    private String generateKey(HttpServletRequest request, RateLimit rateLimit) {
        String key = rateLimit.key();
        String uri = request.getRequestURI();

        switch (rateLimit.limitType()) {
            case IP:
                String ip = getRemoteAddr(request);
                return String.format("%s:%s:%s", key, uri, ip);
            case USER:
                String userId = request.getHeader("userId");
                if (StrUtil.isBlank(userId)) {
                    userId = "anonymous";
                }
                return String.format("%s:%s:%s", key, uri, userId);
            default:
                return String.format("%s:%s", key, uri);
        }
    }

    /**
     * 获取真实IP
     */
    private String getRemoteAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        return ip;
    }
}
