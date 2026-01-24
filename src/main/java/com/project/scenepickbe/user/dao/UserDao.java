package com.project.scenepickbe.user.dao;

import com.project.scenepickbe.user.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDao {

	// 회원가입
	void insertUser(@Param("vo") UserVo vo);

	// 아이디 중복 확인
	boolean existsByUserId(@Param("userId") String userId);

	// 이메일 중복 확인
	boolean existsByEmail(@Param("email") String email);

	// 회원 로그인 정보 조회
	UserVo selectUser(@Param("loginId") String loginId);
}
