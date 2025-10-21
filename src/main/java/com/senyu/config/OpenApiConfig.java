package com.senyu.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger配置
 *
 * @author senyu
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Feed流系统 API文档")
                        .version("1.0.0")
                        .description("类似小红书和抖音的社交媒体Feed流系统，支持推拉结合的混合策略")
                        .contact(new Contact()
                                .name("Senyu")
                                .email("support@senyu.com")
                                .url("https://github.com/senyu/feed-system"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("本地开发环境"),
                        new Server()
                                .url("https://api.production.com/api")
                                .description("生产环境")
                ));
    }
}
