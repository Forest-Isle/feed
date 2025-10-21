package com.senyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Feed发件箱实体（拉模式）
 *
 * @author senyu
 */
@Data
@TableName("feed_outbox")
public class FeedOutbox {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（内容发布者）
     */
    private Long userId;

    /**
     * 内容ID
     */
    private Long postId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
