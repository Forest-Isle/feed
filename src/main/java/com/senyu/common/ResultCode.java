package com.senyu.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 *
 * @author senyu
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    NOT_FOUND(404, "资源不存在"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),

    // 用户相关
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),

    // 内容相关
    POST_NOT_FOUND(2001, "内容不存在"),
    POST_PUBLISH_FAILED(2002, "内容发布失败"),

    // 关注相关
    ALREADY_FOLLOWED(3001, "已关注该用户"),
    NOT_FOLLOWED(3002, "未关注该用户"),
    CANNOT_FOLLOW_SELF(3003, "不能关注自己"),

    // Feed相关
    FEED_LOAD_FAILED(4001, "Feed流加载失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
