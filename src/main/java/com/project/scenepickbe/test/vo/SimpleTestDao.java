package com.project.scenepickbe.test.vo;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SimpleTestDao {
	List<SimpleTestVo> selectSimpleTestList();
}
