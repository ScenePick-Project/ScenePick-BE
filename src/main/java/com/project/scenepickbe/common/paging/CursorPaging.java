package com.project.scenepickbe.common.paging;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;

public final class CursorPaging {

	/**
	 * 두 커서 값이 동시에 있거나 동시에 비어있는지 검증합니다.
	 *
	 * @param primaryCursor 첫 번째 커서 값
	 * @param secondaryCursor 두 번째 커서 값
	 */
	public static void validateCursorPair(Object primaryCursor, Object secondaryCursor) {
		if ((primaryCursor == null) != (secondaryCursor == null)) {
			throw new GeneralException(ErrorStatus.CURSOR_INVALID);
		}
	}

	/**
	 * 요청된 페이지 크기를 기본값 및 최대값 기준으로 보정합니다.
	 *
	 * @param size 요청 크기
	 * @param defaultSize size가 null일 때 기본값
	 * @param maxSize 허용 가능한 최대 크기
	 * @return 보정된 페이지 크기
	 */
	public static int normalizeSize(Integer size, int defaultSize, int maxSize) {
		int resolvedSize = size == null ? defaultSize : size;
		if (resolvedSize <= 0) {
			throw new GeneralException(ErrorStatus.INVALID_PAGE_SIZE);
		}
		return Math.min(resolvedSize, maxSize);
	}

	/**
	 * 페이지 결과 계산용 쿼리 limit을 계산합니다.
	 *
	 * @param pageSize 보정된 페이지 크기
	 * @return 쿼리 limit
	 */
	public static int resolveLimit(int pageSize) {
		return pageSize + 1;
	}

	/**
	 * 조회된 목록에서 현재 페이지 목록을 만들고 다음 페이지로 갈 때 필요한 커서와 존재 여부를 계산합니다.
	 *
	 * @param items 조회된 목록
	 * @param pageSize 보정된 페이지 크기
	 * @param cursorMapper 마지막 항목으로 커서를 만드는 함수
	 * @param <T> 항목 타입
	 * @param <C> 커서 타입
	 * @return 현재 페이지 목록, 다음 커서, 다음 페이지 여부를 포함한 페이지 결과
	 */
	public static <T, C> Slice<T, C> toSlice(List<T> items, int pageSize, Function<T, C> cursorMapper) {
		Objects.requireNonNull(items, "items");
		if (items.isEmpty()) {
			return new Slice<>(items, null, false);
		}

		boolean hasNext = items.size() > pageSize;
		List<T> pageItems = hasNext ? items.subList(0, pageSize) : items;

		C nextCursor = null;
		if (hasNext && !pageItems.isEmpty()) {
			nextCursor = cursorMapper.apply(pageItems.get(pageItems.size() - 1));
		}

		return new Slice<>(pageItems, nextCursor, hasNext);
	}

	/**
	 * 페이지 결과
	 *
	 * @param items 현재 페이지 목록
	 * @param nextCursor 다음 페이지 커서
	 * @param hasNext 다음 페이지 존재 여부
	 * @param <T> 항목 타입
	 * @param <C> 커서 타입
	 */
	public record Slice<T, C>(List<T> items, C nextCursor, boolean hasNext) {
	}
}
