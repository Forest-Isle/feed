package com.senyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Feed收件箱实体（推模式）
 *
 * @author senyu
 */
@Data
@TableName("feed_inbox")
public class FeedInbox {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（Feed接收者）
     */
    private Long userId;

    /**
     * 内容ID
     */
    private Long postId;

    /**
     * 内容作者ID
     */
    private Long authorId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
