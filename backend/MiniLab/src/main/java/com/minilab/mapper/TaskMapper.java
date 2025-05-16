package com.minilab.mapper;

import com.minilab.pojo.entity.Task;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TaskMapper {
    @Select("select * from minilab.task where `group` = #{groupId}")
    List<Task> getTaskByGroupId(String groupId);

    @Select("select * from minilab.task where emp = #{id} and start_time >= #{start} and start_time <= #{end}")
    List<Task> getTaskByIdAndTime(Integer id, LocalDate start, LocalDate end);

    @Update("update minilab.task set is_finish = 1, update_time = now() where id = #{taskId}")
    void solveTask(Integer taskId);

    @Delete("delete from minilab.task where id = #{id}")
    void deleteTaskById(Integer id);

    void updateTask(Task task);
}
