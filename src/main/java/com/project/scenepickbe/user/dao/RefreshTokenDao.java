package com.project.scenepickbe.user.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Mapper
public interface RefreshTokenDao {

	// REFRESH TOKEN 저장
	void insertUserRefreshToken(@Param("userId") String userId, @Param("jti") String jti, @Param("expiresAt") Date expiresAt);

	// 토큰 유효성 검사
	int existsActive(@Param("userId") String userId, @Param("jti") String jti);

	// 토큰 폐기
	void revokeUserRefreshToken(@Param("userId") String userId, @Param("jti") String jti);
}
