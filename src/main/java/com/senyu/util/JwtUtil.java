package com.senyu.util;

import com.senyu.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author senyu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtConfig jwtConfig;

    /**
     * 获取密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成Access Token
     *
     * @param userId       用户ID
     * @param username     用户名
     * @param tokenVersion Token版本号
     * @return JWT token
     */
    public String generateAccessToken(Long userId, String username, Integer tokenVersion) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("tokenVersion", tokenVersion);
        claims.put("tokenType", "access");

        return generateToken(claims, jwtConfig.getAccessTokenExpiration());
    }

    /**
     * 生成Refresh Token
     *
     * @param userId       用户ID
     * @param tokenVersion Token版本号
     * @return JWT token
     */
    public String generateRefreshToken(Long userId, Integer tokenVersion) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("tokenVersion", tokenVersion);
        claims.put("tokenType", "refresh");

        return generateToken(claims, jwtConfig.getRefreshTokenExpiration());
    }

    /**
     * 生成Token
     *
     * @param claims     载荷
     * @param expiration 过期时间（毫秒）
     * @return JWT token
     */
    private String generateToken(Map<String, Object> claims, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .issuer(jwtConfig.getIssuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从Token中获取Claims
     *
     * @param token JWT token
     * @return Claims
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("解析JWT token失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get("username") : null;
    }

    /**
     * 从Token中获取Token版本号
     *
     * @param token JWT token
     * @return Token版本号
     */
    public Integer getTokenVersionFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (Integer) claims.get("tokenVersion") : null;
    }

    /**
     * 从Token中获取Token类型
     *
     * @param token JWT token
     * @return Token类型
     */
    public String getTokenTypeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get("tokenType") : null;
    }

    /**
     * 验证Token是否过期
     *
     * @param token JWT token
     * @return true-未过期，false-已过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return true;
            }
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证Token是否有效
     *
     * @param token        JWT token
     * @param userId       用户ID
     * @param tokenVersion Token版本号
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token, Long userId, Integer tokenVersion) {
        try {
            Long tokenUserId = getUserIdFromToken(token);
            Integer tokenVersionFromToken = getTokenVersionFromToken(token);

            return tokenUserId != null
                    && tokenUserId.equals(userId)
                    && tokenVersionFromToken != null
                    && tokenVersionFromToken.equals(tokenVersion)
                    && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("验证JWT token失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从请求头中提取Token
     *
     * @param headerValue Authorization header值
     * @return JWT token
     */
    public String extractToken(String headerValue) {
        if (headerValue != null && headerValue.startsWith(jwtConfig.getPrefix())) {
            return headerValue.substring(jwtConfig.getPrefix().length());
        }
        return null;
    }
}
