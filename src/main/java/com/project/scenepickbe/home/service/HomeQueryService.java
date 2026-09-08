package com.project.scenepickbe.home.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.scenepickbe.content.dao.ContentDao;
import com.project.scenepickbe.home.dto.response.HomeResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeQueryService {

	private final ContentDao contentDao;

	public HomeResponse.Recommendations getRecommendations() {
		List<HomeResponse.Content> contentList = contentDao.selectRandomContentsForHome().stream()
			.map(content -> new HomeResponse.Content(content.getContentId(), content.getTitle(),
				content.getPosterImageUrl(), content.getContentType()))
			.toList();
		return new HomeResponse.Recommendations("RANDOM", contentList);
	}
}
