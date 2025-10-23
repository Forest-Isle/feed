package com.senyu.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.senyu.common.ResultCode;
import com.senyu.config.JwtConfig;
import com.senyu.dto.*;
import com.senyu.entity.User;
import com.senyu.exception.BusinessException;
import com.senyu.mapper.UserMapper;
import com.senyu.service.UserService;
import com.senyu.util.JwtUtil;
import com.senyu.util.PasswordUtil;
import com.senyu.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 *
 * @author senyu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (getUserByUsername(request.getUsername()) != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        // 检查邮箱是否已存在
        if (getUserByEmail(request.getEmail()) != null) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_EXISTS);
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordUtil.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname());
        user.setFollowerCount(0);
        user.setFollowingCount(0);
        user.setPostCount(0);
        user.setIsActive(true);
        user.setTokenVersion(0);

        userMapper.insert(user);

        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());

        // 自动登录
        return generateAuthResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResponse login(LoginRequest request) {
        // 支持用户名或邮箱登录
        User user = getUserByUsername(request.getUsername());
        if (user == null) {
            user = getUserByEmail(request.getUsername());
        }

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证密码
        if (!passwordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.INVALID_PASSWORD);
        }

        // 检查用户是否活跃
        if (!user.getIsActive()) {
            throw new BusinessException(ResultCode.USER_INACTIVE);
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

        return generateAuthResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 验证refresh token
        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_EXPIRED);
        }

        // 检查token类型
        String tokenType = jwtUtil.getTokenTypeFromToken(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new BusinessException(ResultCode.INVALID_TOKEN);
        }

        // 获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        Integer tokenVersion = jwtUtil.getTokenVersionFromToken(refreshToken);

        if (userId == null || tokenVersion == null) {
            throw new BusinessException(ResultCode.INVALID_TOKEN);
        }

        // 检查token是否在黑名单中
        String blacklistKey = RedisKeyUtil.getTokenBlacklistKey(refreshToken);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            throw new BusinessException(ResultCode.TOKEN_BLACKLISTED);
        }

        User user = getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证token版本
        if (!jwtUtil.validateToken(refreshToken, userId, user.getTokenVersion())) {
            throw new BusinessException(ResultCode.TOKEN_INVALID_VERSION);
        }

        log.info("刷新Token成功: userId={}", userId);

        return generateAuthResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 增加token版本号，使所有旧token失效
        user.setTokenVersion(user.getTokenVersion() + 1);
        userMapper.updateById(user);

        // 清除Redis缓存
        String userCacheKey = RedisKeyUtil.getUserInfoKey(userId);
        redisTemplate.delete(userCacheKey);

        log.info("用户登出成功: userId={}", userId);
    }

    @Override
    public User getUserById(Long userId) {
        if (userId == null) {
            return null;
        }

        // 先从Redis缓存获取
        String cacheKey = RedisKeyUtil.getUserInfoKey(userId);
        User user = (User) redisTemplate.opsForValue().get(cacheKey);

        if (user == null) {
            // 从数据库查询
            user = userMapper.selectById(userId);
            if (user != null) {
                // 存入缓存，过期时间1小时
                redisTemplate.opsForValue().set(cacheKey, user, 1, TimeUnit.HOURS);
            }
        }

        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        if (StrUtil.isBlank(username)) {
            return null;
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public User getUserByEmail(String email) {
        if (StrUtil.isBlank(email)) {
            return null;
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public UserVO convertToVO(User user) {
        if (user == null) {
            return null;
        }

        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .followerCount(user.getFollowerCount())
                .followingCount(user.getFollowingCount())
                .postCount(user.getPostCount())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * 生成认证响应
     *
     * @param user 用户实体
     * @return 认证响应
     */
    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getTokenVersion()
        );

        String refreshToken = jwtUtil.generateRefreshToken(
                user.getId(),
                user.getTokenVersion()
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getAccessTokenExpiration())
                .user(convertToVO(user))
                .build();
    }
}
