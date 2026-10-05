package com.project.scenepickbe.mypage;

import static org.assertj.core.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;

class MyPagePolicyTest {
	@Test
	void ratingsAllowOnlyHalfStarsOrNoRating() {
		MyPageRequest.validateRating(null);
		for (String value : new String[]{"0.5", "1.0", "4.5", "5.0"}) {
			assertThatCode(() -> MyPageRequest.validateRating(new BigDecimal(value))).doesNotThrowAnyException();
		}
		for (String value : new String[]{"0", "5.5", "1.25", "-1"}) {
			assertThatThrownBy(() -> MyPageRequest.validateRating(new BigDecimal(value)))
				.isInstanceOf(GeneralException.class);
		}
	}
}
