-- Feed流系统数据库初始化脚本

CREATE DATABASE IF NOT EXISTS feed_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE feed_system;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名',
    `nickname` VARCHAR(64) NOT NULL COMMENT '昵称',
    `avatar` VARCHAR(512) COMMENT '头像URL',
    `bio` VARCHAR(256) COMMENT '个人简介',
    `follower_count` INT DEFAULT 0 COMMENT '粉丝数',
    `following_count` INT DEFAULT 0 COMMENT '关注数',
    `post_count` INT DEFAULT 0 COMMENT '发帖数',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否活跃用户',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_username (`username`),
    INDEX idx_follower_count (`follower_count`),
    INDEX idx_created_at (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 内容表
CREATE TABLE IF NOT EXISTS `post` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '内容ID',
    `user_id` BIGINT NOT NULL COMMENT '发布用户ID',
    `content` TEXT NOT NULL COMMENT '内容文本',
    `images` JSON COMMENT '图片URL列表',
    `video_url` VARCHAR(512) COMMENT '视频URL',
    `topic` VARCHAR(128) COMMENT '话题标签',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `share_count` INT DEFAULT 0 COMMENT '分享数',
    `view_count` INT DEFAULT 0 COMMENT '浏览数',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-草稿 1-已发布 2-已删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (`user_id`),
    INDEX idx_created_at (`created_at`),
    INDEX idx_status (`status`),
    INDEX idx_topic (`topic`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容表';

-- 关注关系表
CREATE TABLE IF NOT EXISTS `follow` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    `follower_id` BIGINT NOT NULL COMMENT '粉丝ID',
    `followee_id` BIGINT NOT NULL COMMENT '被关注者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_follow (`follower_id`, `followee_id`),
    INDEX idx_follower (`follower_id`),
    INDEX idx_followee (`followee_id`),
    FOREIGN KEY (`follower_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`followee_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注关系表';

-- Feed收件箱表（推模式）
CREATE TABLE IF NOT EXISTS `feed_inbox` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID（Feed接收者）',
    `post_id` BIGINT NOT NULL COMMENT '内容ID',
    `author_id` BIGINT NOT NULL COMMENT '内容作者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_post (`user_id`, `created_at` DESC),
    INDEX idx_post (`post_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`post_id`) REFERENCES `post`(`id`),
    FOREIGN KEY (`author_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Feed收件箱表';

-- 发件箱表（拉模式）
CREATE TABLE IF NOT EXISTS `feed_outbox` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID（内容发布者）',
    `post_id` BIGINT NOT NULL COMMENT '内容ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_post (`user_id`, `created_at` DESC),
    INDEX idx_post (`post_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`post_id`) REFERENCES `post`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发件箱表';

-- 插入测试数据
INSERT INTO `user` (`id`, `username`, `nickname`, `avatar`, `bio`, `follower_count`, `following_count`, `post_count`, `is_active`)
VALUES
(1, 'user001', '小红书博主', 'https://example.com/avatar1.jpg', '分享生活美好瞬间', 500, 200, 50, 1),
(2, 'user002', '抖音达人', 'https://example.com/avatar2.jpg', '记录美好生活', 50000, 100, 200, 1),
(3, 'user003', '普通用户', 'https://example.com/avatar3.jpg', '热爱生活', 10, 50, 5, 1);
