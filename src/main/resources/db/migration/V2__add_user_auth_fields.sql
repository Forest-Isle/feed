-- 为用户表添加认证相关字段
-- 版本: V2
-- 描述: 添加JWT认证所需的字段（password, email, phone, token_version, last_login_at）

ALTER TABLE `user`
    ADD COLUMN `password` VARCHAR(255) NULL COMMENT '密码（BCrypt加密）' AFTER `username`,
    ADD COLUMN `email` VARCHAR(100) NULL COMMENT '邮箱' AFTER `password`,
    ADD COLUMN `phone` VARCHAR(20) NULL COMMENT '手机号' AFTER `email`,
    ADD COLUMN `token_version` INT NOT NULL DEFAULT 0 COMMENT 'Token版本号' AFTER `is_active`,
    ADD COLUMN `last_login_at` DATETIME NULL COMMENT '最后登录时间' AFTER `token_version`;

-- 为email创建唯一索引
ALTER TABLE `user` ADD UNIQUE INDEX `uk_email` (`email`);

-- 为phone创建索引
ALTER TABLE `user` ADD INDEX `idx_phone` (`phone`);

-- 为last_login_at创建索引（用于查询活跃用户）
ALTER TABLE `user` ADD INDEX `idx_last_login` (`last_login_at`);
