package com.project.scenepickbe.moment.service;

import org.springframework.stereotype.Service;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.common.paging.CursorPaging;
import com.project.scenepickbe.moment.dao.MomentDao;
import com.project.scenepickbe.moment.dto.request.MomentRequest;
import com.project.scenepickbe.moment.dto.response.MomentResponse;
import com.project.scenepickbe.moment.vo.MomentVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MomentQueryService {
	private static final int DEFAULT_SIZE = 10;
	private static final int MAX_SIZE = 30;

	private final MomentDao momentDao;

	public MomentResponse.Detail getMoment(Long momentId, String userId) {
		MomentVo moment = momentDao.selectMoment(momentId, userId);
		if (moment == null) {
			throw new GeneralException(ErrorStatus.MOMENT_NOT_FOUND);
		}
		return MomentResponse.Detail.from(moment);
	}

	public MomentResponse.Slice getMoments(String userId, MomentRequest.Slice request) {
		CursorPaging.validateCursorPair(request.cursorCreatedAt(), request.cursorMomentId());
		int pageSize = CursorPaging.normalizeSize(request.size(), DEFAULT_SIZE, MAX_SIZE);
		var moments = momentDao.selectMomentCursor(userId, request.contentId(), request.youtubeId(),
			request.cursorCreatedAt(), request.cursorMomentId(), CursorPaging.resolveLimit(pageSize));
		var page = CursorPaging.toSlice(moments, pageSize,
			moment -> new MomentResponse.Cursor(moment.getCreatedAt(), moment.getMomentId()));
		return new MomentResponse.Slice(page.items().stream().map(MomentResponse.Detail::from).toList(),
			page.nextCursor(), page.hasNext());
	}
}
