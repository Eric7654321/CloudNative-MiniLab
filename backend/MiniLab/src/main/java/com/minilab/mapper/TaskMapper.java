package com.minilab.mapper;

import com.minilab.pojo.entity.Task;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TaskMapper {
    @Select("select * from minilab.task where `group` = #{groupId}")
    List<Task> getTaskByGroupId(String groupId);

    @Select("select * from minilab.task where emp = #{id} AND NOT (end_time <= #{start} OR start_time >= #{end}) and is_finish != 1")
    List<Task> getTaskByIdAndTime(Integer id, LocalDate start, LocalDate end);

    @Select("select * from minilab.task where emp = #{id} and is_finish != 1")
    List<Task> getTaskById(Integer id);

    @Update("update minilab.task set is_finish = 1, update_time = now() where id = #{taskId}")
    void solveTask(Integer taskId);

    @Delete("delete from minilab.task where id = #{id}")
    void deleteTaskById(Integer id);

    void updateTask(Task task);
    @Insert("insert into minilab.task (emp, emp_name, machine, machine_name, start_time, end_time,tag, description, `group`, is_finish, update_time, updater_id) " +
            "VALUES (#{emp}, #{empName}, #{machine}, #{machineName}, #{startTime}, #{endTime},#{tag}, #{description}, #{group}, #{isFinish}, #{updateTime}, #{updaterId})")
    void insertTask(Task task);

    @Select("select * from minilab.task where emp_name = #{empName}")
    Task selectTaskByEmpName(String empName);
}
