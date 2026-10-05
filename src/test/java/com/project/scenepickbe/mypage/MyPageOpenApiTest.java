package com.project.scenepickbe.mypage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;

class MyPageOpenApiTest {
	@Test
	void listSchemasKeepConcreteItemTypes() throws Exception {
		for (String method : new String[] {"reviews", "bookmarks", "following", "followers", "tracks"}) {
			var type = MyPageController.class.getMethod(method, User.class, MyPageRequest.Page.class).getGenericReturnType();
			var resolved = ModelConverters.getInstance().resolveAsResolvedSchema(new AnnotatedType(type).resolveAsRef(true));
			io.swagger.v3.oas.models.media.Schema<?> wrapper = resolved.referencedSchemas.get(resolved.schema.get$ref().substring("#/components/schemas/".length()));
			io.swagger.v3.oas.models.media.Schema<?> page = resolved.referencedSchemas.get(wrapper.getProperties().get("result").get$ref().substring("#/components/schemas/".length()));
			String expected = method.equals("tracks") ? "TrackItem" : method.equals("following") || method.equals("followers") ? "UserItem" : "ReviewItem";
			assertThat(page.getProperties().get("items").getItems().get$ref()).isEqualTo("#/components/schemas/" + expected);
			assertThat(page.getProperties()).containsKeys("nextCursor", "hasNext");
		}
	}
}
