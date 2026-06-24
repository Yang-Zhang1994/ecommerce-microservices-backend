/**
 * Springdoc OpenAPI 3 config (replaces Springfox Swagger 2).
 */
package io.renren.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("人人开源")
                .description("renren-fast API")
                .termsOfService("https://www.renren.io")
                .version("3.0.0"))
            .addSecurityItem(new SecurityRequirement().addList("token"))
            .schemaRequirement("token",
                new SecurityScheme()
                    .name("token")
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER));
    }
}
