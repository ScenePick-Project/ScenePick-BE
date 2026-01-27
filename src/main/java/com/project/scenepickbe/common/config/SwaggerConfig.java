package com.project.scenepickbe.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	private static final String COOKIE_SCHEME = "cookieAuth";

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.info(apiInfo())
			.components(new Components().addSecuritySchemes(
				COOKIE_SCHEME,
				new SecurityScheme()
					.type(SecurityScheme.Type.APIKEY)
					.in(SecurityScheme.In.COOKIE)
					.name("access_token")
			))
			.addSecurityItem(new SecurityRequirement().addList(COOKIE_SCHEME));
	}

	private Info apiInfo() {
		return new Info()
			.title("ScenePick API Documentation")
			.description("ScenePick 프로젝트의 API 명세서입니다.")
			.version("1.0.0");
	}
}
