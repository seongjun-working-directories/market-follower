package com.example.market_follower.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(
                new Info().title("Market Follower - 주식 시세 스트리밍 서버")
                        .version("v1.0")
                        .description("Market Follower 백엔드 API 명세서")
        );
    }
}
