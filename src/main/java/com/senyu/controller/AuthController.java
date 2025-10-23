package com.senyu.controller;

import com.senyu.annotation.RateLimit;
import com.senyu.common.Result;
import com.senyu.dto.*;
import com.senyu.interceptor.JwtAuthenticationInterceptor;
import com.senyu.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author senyu
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户注册、登录、登出等认证相关接口")
public class AuthController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册")
    @RateLimit(key = "register", count = 5, time = 3600, limitType = RateLimit.LimitType.IP)
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册请求: username={}", request.getUsername());
        AuthResponse response = userService.register(request);
        return Result.success(response);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取访问令牌")
    @RateLimit(key = "login", count = 10, time = 300, limitType = RateLimit.LimitType.IP)
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录请求: username={}", request.getUsername());
        AuthResponse response = userService.login(request);
        return Result.success(response);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用刷新令牌获取新的访问令牌")
    @RateLimit(key = "refresh", count = 20, time = 60, limitType = RateLimit.LimitType.DEFAULT)
    public Result<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("刷新Token请求");
        AuthResponse response = userService.refreshToken(request);
        return Result.success(response);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出，使当前Token失效")
    public Result<Void> logout(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(JwtAuthenticationInterceptor.USER_ID_ATTRIBUTE);
        log.info("用户登出请求: userId={}", userId);
        userService.logout(userId);
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public Result<UserVO> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(JwtAuthenticationInterceptor.USER_ID_ATTRIBUTE);
        log.info("获取当前用户信息: userId={}", userId);
        UserVO user = userService.convertToVO(userService.getUserById(userId));
        return Result.success(user);
    }
}
