package com.ssafy.home.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SSAFY_HOME 3조")
                        .version("1.0.0")
                        .description("SSAFY 부동산 프로젝트 API 문서")
                        .contact(new Contact()
                                .name("SSAFY")
                                .url("https://github.com/ijip-naejip/backend")
                        ));
    }
}

