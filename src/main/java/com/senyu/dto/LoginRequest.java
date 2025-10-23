package com.senyu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求DTO
 *
 * @author senyu
 */
@Data
@Schema(description = "用户登录请求")
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名或邮箱", example = "john_doe")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "password123")
    private String password;
}
