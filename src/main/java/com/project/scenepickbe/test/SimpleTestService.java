package com.project.scenepickbe.test;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.project.scenepickbe.vo.SimpleTestDao;
import com.project.scenepickbe.vo.SimpleTestVo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimpleTestService {

	private final SimpleTestDao simpleTestDao;
	private final ModelMapper modelMapper;

	public SimpleTestListResDto getSimpleTestList() {
		List<SimpleTestVo> voList = simpleTestDao.selectSimpleTestList();

		List<SimpleTestListResDto.SimpleTestListDetailResDto> dataList = voList.stream()
			.map(data -> modelMapper.map(data, SimpleTestListResDto.SimpleTestListDetailResDto.class))
			.collect(Collectors.toList());

		return SimpleTestListResDto.builder()
			.totalCount(1)
			.dataList(dataList)
			.build();
	}
}
