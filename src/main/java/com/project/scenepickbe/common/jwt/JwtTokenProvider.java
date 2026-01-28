package com.project.scenepickbe.common.jwt;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.common.jwt.dto.JwtToken;
import com.project.scenepickbe.common.jwt.dto.RefreshPayload;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenProvider {

	public static final String GRANT_TYPE = "Bearer";

	private static final String CLAIM_AUTH = "auth";
	private static final String CLAIM_TYPE = "type";
	private static final String CLAIM_JTI = "jti";

	private static final String TYPE_ACCESS = "access";
	private static final String TYPE_REFRESH = "refresh";

	private final SecretKey secretKey;
	private final long accessExpMs;
	private final long refreshExpMs;

	public JwtTokenProvider(@Value("${jwt.secret}") String secret,
		@Value("${jwt.access-expiration-minutes}") long accessTokenExpiration,
		@Value("${jwt.refresh-expiration-days}") long refreshTokenExpiration) {
		byte[] keyBytes = Decoders.BASE64.decode(secret);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
		this.accessExpMs = Duration.ofMinutes(accessTokenExpiration).toMillis();
		this.refreshExpMs = Duration.ofDays(refreshTokenExpiration).toMillis();
	}

	// 로그인 성공 시 토큰 발급
	public JwtToken generateToken(String userId, Collection<? extends GrantedAuthority> authorities) {
		long now = System.currentTimeMillis();

		String authStr = authorities.stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		// accessToken 생성
		String accessToken = createAccessTokenInternal(userId, authStr, now);

		// refresh 토큰 생성
		String refreshJti = UUID.randomUUID().toString();
		String refreshToken = createRefreshTokenInternal(userId, refreshJti, now);

		return JwtToken.builder()
			.grantType(GRANT_TYPE)
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.refreshJti(refreshJti)
			.build();
	}

	// Access 토큰 생성 내부 구현
	private String createAccessTokenInternal(String userId, String authStr, long now) {
		return baseBuilder(userId, now, now + accessExpMs)
			.claim(CLAIM_TYPE, TYPE_ACCESS)
			.claim(CLAIM_AUTH, authStr)
			.compact();
	}

	// Refresh 토큰 생성 내부 구현
	private String createRefreshTokenInternal(String userId, String jti, long now) {
		return baseBuilder(userId, now, now + refreshExpMs)
			.claim(CLAIM_TYPE, TYPE_REFRESH)
			.claim(CLAIM_JTI, jti)
			.compact();
	}

	// Access/Refresh 공통으로 사용하는 JWT 빌더 베이스
	private JwtBuilder baseBuilder(String subject, long issuedAtMs, long expMs) {
		return Jwts.builder()
			.subject(subject)
			.issuedAt(new Date(issuedAtMs))
			.expiration(new Date(expMs))
			.signWith(secretKey);
	}

	// JWT를 복호화하여 토큰에 있는 정보를 꺼내 생성
	public Authentication getAuthentication(String token) {
		Claims claims = parseClaims(token);

		// access만 인증 생성 허용
		String type = claims.get(CLAIM_TYPE, String.class);
		if (!TYPE_ACCESS.equals(type)) {
			throw new GeneralException(ErrorStatus.JWT_NOT_ACCESS);
		}

		// 권한 확인
		String auth = claims.get(CLAIM_AUTH, String.class);
		if (auth == null || auth.isBlank()) {
			throw new GeneralException(ErrorStatus.JWT_NO_AUTH);
		}

		// 클레임에서 권한을 갖고 옴
		Collection<? extends GrantedAuthority> authorities =
			Arrays.stream(auth.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		UserDetails principal = new User(claims.getSubject(), "", authorities);
		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}

	// Refresh 토큰 파싱
	public RefreshPayload parseRefreshToken(String refreshToken) {
		Claims claims = parseClaims(refreshToken);

		String type = claims.get(CLAIM_TYPE, String.class);
		if (!TYPE_REFRESH.equals(type)) {
			throw new RuntimeException("Refresh Token이 아닙니다.");
		}

		return new RefreshPayload(
			claims.getSubject(),
			claims.get(CLAIM_JTI, String.class),
			claims.getExpiration()
		);
	}

	// 서명/만료 기준 유효성 검사
	public boolean validateToken(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (RuntimeException e) {
			log.warn("유효하지 않은 토큰: {}", e.getMessage());
			return false;
		}
	}

	// JWT를 파싱해서 클레임 정보를 반환
	private Claims parseClaims(String accessToken) {
		try {
			return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(accessToken)
				.getPayload();
		} catch (ExpiredJwtException e) {
			throw new GeneralException(ErrorStatus.JWT_EXPIRED);
		} catch (JwtException | IllegalArgumentException e) {
			throw new GeneralException(ErrorStatus.JWT_INVALID);
		}
	}
}
