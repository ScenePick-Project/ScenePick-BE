package com.project.scenepickbe.test.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("SimpleTestVo")
public class SimpleTestVo {
	private Long id;
	private String name;
}
