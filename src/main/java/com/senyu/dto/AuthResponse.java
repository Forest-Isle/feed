package com.senyu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应DTO
 *
 * @author senyu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "认证响应")
public class AuthResponse {

    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "访问令牌过期时间（毫秒）", example = "7200000")
    private Long expiresIn;

    @Schema(description = "用户信息")
    private UserVO user;
}
