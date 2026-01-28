package com.project.scenepickbe.common.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import com.project.scenepickbe.common.swagger.DocSuccess;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

@Configuration
public class OpenApiDocCustomizerConfig {

	@Bean
	public OperationCustomizer docSuccessOperationCustomizer() {
		return (Operation operation, HandlerMethod handlerMethod) -> {
			DocSuccess docSuccess = handlerMethod.getMethodAnnotation(DocSuccess.class);
			if (docSuccess == null) {
				return operation;
			}

			Schema<?> resultSchema = ModelConverters.getInstance()
				.resolveAsResolvedSchema(new AnnotatedType(docSuccess.value()).resolveAsRef(true))
				.schema;

			Schema<?> wrapperSchema = new ObjectSchema()
				.addProperty("isSuccess", new BooleanSchema().example(true))
				.addProperty("code", new StringSchema().example("COMMON200"))
				.addProperty("message", new StringSchema().example("Success"))
				.addProperty("result", resultSchema);

			MediaType mediaType = new MediaType().schema(wrapperSchema);
			Content content = new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
				mediaType);
			ApiResponse apiResponse = new ApiResponse().description("성공").content(content);

			ApiResponses responses = operation.getResponses();
			if (responses == null) {
				responses = new ApiResponses();
				operation.setResponses(responses);
			}
			responses.addApiResponse("200", apiResponse);

			return operation;
		};
	}

	@Bean
	public OpenApiCustomizer docSuccessOpenApiCustomiser(
		ObjectProvider<org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping> handlerMappingProvider) {
		return openApi -> {
			org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping handlerMapping =
				handlerMappingProvider.getIfAvailable();
			if (handlerMapping == null) {
				return;
			}

			Components components = openApi.getComponents();
			if (components == null) {
				components = new Components();
				openApi.setComponents(components);
			}

			Map<String, Schema> existingSchemas = components.getSchemas();
			if (existingSchemas == null) {
				existingSchemas = new LinkedHashMap<>();
				components.setSchemas(existingSchemas);
			}
			final Map<String, Schema> schemas = existingSchemas;

			handlerMapping.getHandlerMethods().values().forEach(handlerMethod -> {
				DocSuccess docSuccess = handlerMethod.getMethodAnnotation(DocSuccess.class);
				if (docSuccess == null) {
					return;
				}

				ResolvedSchema resolved = ModelConverters.getInstance()
					.resolveAsResolvedSchema(new AnnotatedType(docSuccess.value()).resolveAsRef(true));

				if (resolved.referencedSchemas != null) {
					resolved.referencedSchemas.forEach(schemas::putIfAbsent);
				}

				if (resolved.schema != null && resolved.schema.get$ref() == null) {
					String name = docSuccess.value().getSimpleName();
					schemas.putIfAbsent(name, resolved.schema);
				}
			});
		};
	}
}
