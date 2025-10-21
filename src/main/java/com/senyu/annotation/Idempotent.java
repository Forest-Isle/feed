package com.senyu.annotation;

import java.lang.annotation.*;

/**
 * 幂等性注解
 * 用于防止重复提交
 *
 * @author senyu
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 幂等性key的前缀
     */
    String prefix() default "idempotent";

    /**
     * 幂等性有效期（秒）
     */
    int expireTime() default 300;

    /**
     * 提示信息
     */
    String message() default "请勿重复提交";
}
