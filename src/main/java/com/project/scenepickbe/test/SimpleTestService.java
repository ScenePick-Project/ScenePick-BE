package com.project.scenepickbe.test;

import com.project.scenepickbe.test.vo.SimpleTestDao;
import com.project.scenepickbe.test.vo.SimpleTestVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
