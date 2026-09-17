package com.project.scenepickbe.moment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.moment.dao.MomentDao;
import com.project.scenepickbe.moment.dto.request.MomentRequest;
import com.project.scenepickbe.moment.dto.response.MomentResponse;
import com.project.scenepickbe.moment.vo.MomentVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MomentCommandService {
	private final MomentDao momentDao;

	@Transactional
	public MomentResponse.Created createMoment(Long contentId, String userId, MomentRequest.Create request) {
		validateRange(request.startTime(), request.endTime());
		if (momentDao.existsContent(contentId) == 0) {
			throw new GeneralException(ErrorStatus.CONTENT_NOT_FOUND);
		}
		MomentVo moment = new MomentVo();
		moment.setContentId(contentId);
		moment.setUserId(userId);
		moment.setYoutubeId(request.youtubeId());
		moment.setStartTime(request.startTime());
		moment.setEndTime(request.endTime());
		moment.setMemo(normalizeMemo(request.memo()));
		momentDao.insertMoment(moment);
		return new MomentResponse.Created(moment.getMomentId());
	}

	@Transactional
	public MomentResponse.Detail updateMoment(Long momentId, String userId, MomentRequest.Update request) {
		MomentVo moment = momentDao.selectMomentForUpdate(momentId, userId);
		if (moment == null) {
			throw new GeneralException(ErrorStatus.MOMENT_NOT_FOUND);
		}
		if (request.getStartTime() == null && request.getEndTime() == null && !request.isMemoProvided()) {
			return MomentResponse.Detail.from(moment);
		}
		Integer startTime = request.getStartTime() == null ? moment.getStartTime() : request.getStartTime();
		Integer endTime = request.getEndTime() == null ? moment.getEndTime() : request.getEndTime();
		validateRange(startTime, endTime);
		moment.setStartTime(startTime);
		moment.setEndTime(endTime);
		if (request.isMemoProvided()) {
			moment.setMemo(normalizeMemo(request.getMemo()));
		}
		if (momentDao.updateMoment(moment) == 0) {
			throw new GeneralException(ErrorStatus.MOMENT_NOT_FOUND);
		}
		return MomentResponse.Detail.from(momentDao.selectMoment(momentId, userId));
	}

	@Transactional
	public void deleteMoment(Long momentId, String userId) {
		if (momentDao.deleteMoment(momentId, userId) == 0) {
			throw new GeneralException(ErrorStatus.MOMENT_NOT_FOUND);
		}
	}

	private String normalizeMemo(String memo) {
		return memo == null || memo.isEmpty() ? null : memo;
	}

	private void validateRange(Integer startTime, Integer endTime) {
		if (startTime == null || endTime == null || startTime < 0 || endTime <= startTime) {
			throw new GeneralException(ErrorStatus._BAD_REQUEST);
		}
	}
}
