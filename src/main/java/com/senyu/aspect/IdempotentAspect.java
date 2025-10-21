package com.senyu.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.senyu.annotation.Idempotent;
import com.senyu.exception.BusinessException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * 幂等性切面
 *
 * @author senyu
 */
@Slf4j
@Aspect
@Component
public class IdempotentAspect {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String userId = request.getHeader("userId");
        if (StrUtil.isBlank(userId)) {
            userId = "anonymous";
        }

        // 生成幂等性key
        String idempotentKey = generateKey(joinPoint, idempotent.prefix(), userId);

        // 尝试设置key，如果key已存在则说明是重复请求
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", idempotent.expireTime(), TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(success)) {
            log.warn("检测到重复请求，key: {}", idempotentKey);
            throw new BusinessException(idempotent.message());
        }

        try {
            // 执行业务逻辑
            return joinPoint.proceed();
        } catch (Exception e) {
            // 业务执行失败，删除幂等性key，允许重试
            redisTemplate.delete(idempotentKey);
            throw e;
        }
    }

    /**
     * 生成幂等性key
     */
    private String generateKey(ProceedingJoinPoint joinPoint, String prefix, String userId) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();

        // 参数转JSON
        Object[] args = joinPoint.getArgs();
        String argsJson = JSONUtil.toJsonStr(args);

        // MD5加密参数，避免key过长
        String argsMd5 = DigestUtil.md5Hex(argsJson);

        return String.format("%s:%s:%s:%s:%s", prefix, className, methodName, userId, argsMd5);
    }
}
