package com.senyu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置类
 *
 * @author senyu
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * JWT密钥
     */
    private String secret = "your-256-bit-secret-key-change-this-in-production-environment";

    /**
     * Access Token过期时间（毫秒）
     * 默认2小时
     */
    private Long accessTokenExpiration = 7200000L;

    /**
     * Refresh Token过期时间（毫秒）
     * 默认7天
     */
    private Long refreshTokenExpiration = 604800000L;

    /**
     * Token发行者
     */
    private String issuer = "feed-system";

    /**
     * Token请求头名称
     */
    private String header = "Authorization";

    /**
     * Token前缀
     */
    private String prefix = "Bearer ";
}
