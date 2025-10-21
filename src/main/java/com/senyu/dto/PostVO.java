package com.senyu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容展示DTO
 *
 * @author senyu
 */
@Data
public class PostVO {

    /**
     * 内容ID
     */
    private Long id;

    /**
     * 发布用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 内容文本
     */
    private String content;

    /**
     * 图片URL列表
     */
    private List<String> images;

    /**
     * 视频URL
     */
    private String videoUrl;

    /**
     * 话题标签
     */
    private String topic;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 分享数
     */
    private Integer shareCount;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 是否已点赞
     */
    private Boolean liked;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
