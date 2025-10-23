package com.senyu.config;

import com.senyu.interceptor.JwtAuthenticationInterceptor;
import com.senyu.interceptor.LogInterceptor;
import com.senyu.interceptor.RateLimitInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Web MVC配置
 *
 * @author senyu
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private LogInterceptor logInterceptor;

    @Resource
    private RateLimitInterceptor rateLimitInterceptor;

    @Resource
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 日志拦截器 - 最先执行，记录所有请求
        registry.addInterceptor(logInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**")
                .order(1);

        // JWT认证拦截器 - 第二执行，验证身份
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/register",    // 注册接口
                        "/api/auth/login",       // 登录接口
                        "/api/auth/refresh",     // 刷新token接口
                        "/actuator/**",          // 监控接口
                        "/swagger-ui/**",        // Swagger UI
                        "/v3/api-docs/**"        // API文档
                )
                .order(2);

        // 限流拦截器 - 最后执行，控制频率
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/actuator/**")
                .order(3);
    }

    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
