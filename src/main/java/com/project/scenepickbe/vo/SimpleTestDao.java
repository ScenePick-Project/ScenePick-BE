package com.project.scenepickbe.vo;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SimpleTestDao {
	List<SimpleTestVo> selectSimpleTestList();
}
