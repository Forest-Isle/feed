package com.senyu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户视图对象
 *
 * @author senyu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息")
public class UserVO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "john_doe")
    private String username;

    @Schema(description = "邮箱", example = "john@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "昵称", example = "John")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "个人简介", example = "Hello, I'm John!")
    private String bio;

    @Schema(description = "粉丝数", example = "100")
    private Integer followerCount;

    @Schema(description = "关注数", example = "50")
    private Integer followingCount;

    @Schema(description = "发帖数", example = "20")
    private Integer postCount;

    @Schema(description = "是否活跃", example = "true")
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private LocalDateTime createdAt;
}
