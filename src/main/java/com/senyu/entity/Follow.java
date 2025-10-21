package com.senyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 关注关系实体
 *
 * @author senyu
 */
@Data
@TableName("follow")
public class Follow {

    /**
     * 关系ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 粉丝ID（关注者）
     */
    private Long followerId;

    /**
     * 被关注者ID
     */
    private Long followeeId;

    /**
     * 关注时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;
}
