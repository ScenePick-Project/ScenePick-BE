package com.project.scenepickbe.user.vo;

import com.project.scenepickbe.common.BaseVo;
import com.project.scenepickbe.user.enums.AuthProvider;
import com.project.scenepickbe.user.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@Alias("UserVo")
@AllArgsConstructor
public class UserVo extends BaseVo {

	/* 회원 아이디 */
	private String userId;

	/* 회원 이메일 */
	private String email;

	/* 이름 */
	private String username;

	/* 비밀번호 */
	private String password;

	/* 전화번호 */
	private String phone;

	/* 정지여부 */
	private String banYn;

	/* 역할(권한) */
	private Role role;

	/* 생성일 */
	private Date createAt;

	/* 수정일 */
	private Date updateAt;

	/* 탈퇴여부 */
	private String delYn;

	/* 탈퇴일 */
	private String deleteAt;

	/* OAuth 인증 제공자 */
	private AuthProvider provider;

	/* 인증 제공자로부터 받은 사용자 고유 식별자 */
	private String providerId;

	/* 검색 */
	private Search search;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Search {
		/* 통합검색 */
		private String searchIntegrateValue;
	}
}
