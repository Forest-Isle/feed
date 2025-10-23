package com.senyu.interceptor;

import cn.hutool.core.util.StrUtil;
import com.senyu.common.ResultCode;
import com.senyu.config.JwtConfig;
import com.senyu.entity.User;
import com.senyu.exception.BusinessException;
import com.senyu.service.UserService;
import com.senyu.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器
 *
 * @author senyu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    private final UserService userService;

    /**
     * 用户ID请求属性名
     */
    public static final String USER_ID_ATTRIBUTE = "userId";

    /**
     * 用户名请求属性名
     */
    public static final String USERNAME_ATTRIBUTE = "username";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头获取Token
        String authHeader = request.getHeader(jwtConfig.getHeader());

        if (StrUtil.isBlank(authHeader)) {
            log.warn("请求缺少Token: {}", request.getRequestURI());
            throw new BusinessException(ResultCode.TOKEN_MISSING);
        }

        // 提取Token
        String token = jwtUtil.extractToken(authHeader);
        if (StrUtil.isBlank(token)) {
            log.warn("Token格式不正确: {}", request.getRequestURI());
            throw new BusinessException(ResultCode.INVALID_TOKEN);
        }

        // 检查Token是否过期
        if (jwtUtil.isTokenExpired(token)) {
            log.warn("Token已过期: {}", request.getRequestURI());
            throw new BusinessException(ResultCode.TOKEN_EXPIRED);
        }

        // 获取Token中的用户信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        Integer tokenVersion = jwtUtil.getTokenVersionFromToken(token);
        String tokenType = jwtUtil.getTokenTypeFromToken(token);

        if (userId == null || tokenVersion == null) {
            log.warn("Token信息不完整: {}", request.getRequestURI());
            throw new BusinessException(ResultCode.INVALID_TOKEN);
        }

        // 只允许access token访问接口（refresh token仅用于刷新）
        if (!"access".equals(tokenType)) {
            log.warn("Token类型不正确: {}, tokenType={}", request.getRequestURI(), tokenType);
            throw new BusinessException(ResultCode.INVALID_TOKEN);
        }

        // 验证用户是否存在
        User user = userService.getUserById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查用户是否活跃
        if (!user.getIsActive()) {
            log.warn("用户已被禁用: userId={}", userId);
            throw new BusinessException(ResultCode.USER_INACTIVE);
        }

        // 验证Token版本
        if (!jwtUtil.validateToken(token, userId, user.getTokenVersion())) {
            log.warn("Token版本不匹配: userId={}, tokenVersion={}, userTokenVersion={}",
                    userId, tokenVersion, user.getTokenVersion());
            throw new BusinessException(ResultCode.TOKEN_INVALID_VERSION);
        }

        // 将用户信息存入请求属性，供后续使用
        request.setAttribute(USER_ID_ATTRIBUTE, userId);
        request.setAttribute(USERNAME_ATTRIBUTE, username);

        log.debug("JWT认证通过: userId={}, username={}, uri={}", userId, username, request.getRequestURI());

        return true;
    }
}
