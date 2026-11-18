package com.xq.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档配置（springdoc-openapi）
 *
 * 原 springfox 2.9.2 与 Spring Boot 3 不兼容，已整体替换为 springdoc-openapi。
 * 访问地址：
 *   Swagger UI : http://localhost:8089/swagger-ui.html
 *   OpenAPI JSON: http://localhost:8089/v3/api-docs
 *
 * 注：本项目未使用 @Api/@ApiOperation 等 swagger2 注解，因此接口会以
 * Controller 分组 + 方法签名自动生成，无需额外标注。
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI gymOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("健身房管理系统接口文档")
                        .description("GymSystem 后端 REST API 文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("GymSystem")
                                .email("support@gymsystem.local")));
    }
}
