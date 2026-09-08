package com.project.scenepickbe.moment.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.scenepickbe.moment.vo.MomentVo;

@Mapper
public interface MomentDao {
	int existsContent(Long contentId);

	int insertMoment(MomentVo moment);

	MomentVo selectMomentForUpdate(@Param("momentId") Long momentId, @Param("userId") String userId);

	int updateMoment(MomentVo moment);

	int deleteMoment(@Param("momentId") Long momentId, @Param("userId") String userId);

	MomentVo selectMoment(@Param("momentId") Long momentId, @Param("userId") String userId);

	List<MomentVo> selectMomentCursor(@Param("userId") String userId, @Param("contentId") Long contentId,
		@Param("youtubeId") String youtubeId, @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
		@Param("cursorMomentId") Long cursorMomentId, @Param("limit") int limit);
}
