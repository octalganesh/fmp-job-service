package com.octal.fsm.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        List<Server> servers = List.of(
                new Server().url("http://localhost:8765/api/job-service")
        );

        return new OpenAPI()
                .info(new Info().title("Job service API").version("v1"))
                .servers(servers)
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        //.addSecuritySchemes(securitySchemeName, new SecurityScheme()
                              //  .name(securitySchemeName)
                                //.type(SecurityScheme.Type.HTTP)
                                //.scheme("bearer")
                                //.bearerFormat("JWT")));
                );
    }
}


