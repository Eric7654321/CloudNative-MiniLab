package com.minilab.mapper;

import com.minilab.pojo.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskMapper {
    @Select("select * from minilab.task where `group` = #{groupId}")
    List<Task> getTaskByGroupId(String groupId);
}
