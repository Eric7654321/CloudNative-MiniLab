package com.minilab.mapper;

import com.minilab.pojo.entity.Machine;
import com.minilab.pojo.vo.EmpVO;
import com.minilab.pojo.vo.MachineVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MachineMapper {

    @Select("select * from minilab.machine where `group` = #{groupId}")
    List<MachineVO> getMachineByGroupId(String groupId);

//    @Select("select * from minilab.machine where id = #{id}")
//    Machine getMachineById(Integer id);

    List<Machine> getMachinesByIds(@Param("ids") List<Integer> ids);

    @Select("select tags from minilab.machine_tag where machine_id = #{id}")
    String setTagsByUserId(Integer id);

    @Insert("insert into minilab.machine (name, machine_name, usable, `group`, update_time) VALUES (#{name}, #{machineName}, #{usable}, #{group}, #{updateTime})")
    void insert(Machine machine);

    void updateMachine(Machine machine);

    @Delete("delete from minilab.machine where id = #{id}")
    void deleteMachineById(Machine machine);

    @Select("select * from minilab.machine where name = #{name}")
    Machine getMachineByName(String name);
}
