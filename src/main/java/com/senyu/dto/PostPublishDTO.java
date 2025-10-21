package com.senyu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 发布内容DTO
 *
 * @author senyu
 */
@Data
public class PostPublishDTO {

    /**
     * 发布用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 内容文本
     */
    @NotBlank(message = "内容不能为空")
    @Size(min = 1, max = 5000, message = "内容长度必须在1-5000个字符之间")
    private String content;

    /**
     * 图片URL列表
     */
    @Size(max = 9, message = "最多上传9张图片")
    private List<@Pattern(regexp = "^https?://.*", message = "图片URL格式不正确") String> images;

    /**
     * 视频URL
     */
    @Pattern(regexp = "^(https?://.*)?$", message = "视频URL格式不正确")
    private String videoUrl;

    /**
     * 话题标签
     */
    @Size(max = 50, message = "话题长度不能超过50个字符")
    private String topic;
}
