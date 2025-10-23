package com.senyu.service;

import com.senyu.dto.*;
import com.senyu.entity.User;

/**
 * 用户服务接口
 *
 * @author senyu
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 认证响应
     */
    AuthResponse register(RegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 认证响应
     */
    AuthResponse login(LoginRequest request);

    /**
     * 刷新Token
     *
     * @param request 刷新Token请求
     * @return 认证响应
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * 登出
     *
     * @param userId 用户ID
     */
    void logout(Long userId);

    /**
     * 根据用户ID获取用户
     *
     * @param userId 用户ID
     * @return 用户实体
     */
    User getUserById(Long userId);

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    User getUserByUsername(String username);

    /**
     * 根据邮箱获取用户
     *
     * @param email 邮箱
     * @return 用户实体
     */
    User getUserByEmail(String email);

    /**
     * 将User实体转换为UserVO
     *
     * @param user 用户实体
     * @return 用户视图对象
     */
    UserVO convertToVO(User user);
}
