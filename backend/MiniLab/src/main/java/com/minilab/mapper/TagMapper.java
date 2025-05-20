package com.minilab.mapper;

import com.minilab.pojo.entity.EmpTag;
import com.minilab.pojo.entity.MachineTag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.springframework.web.bind.annotation.DeleteMapping;

@Mapper
public interface TagMapper {
    @Insert("insert into minilab.emp_tag (emp_id) values (#{id})")
    void initialEmpTag(Integer id);

    @Update("update minilab.emp_tag set tags = #{tags}, update_time = #{updateTime} where emp_id = #{empId}")
    void updateEmpTagById(EmpTag tag);

    @Delete("delete from minilab.emp_tag where emp_id = #{id}")
    void deleteTagByEmpId(Integer id);

    @Insert("insert into minilab.machine_tag (machine_id) values (#{id})")
    void initialMachineTag(Integer id);

    @Update("update minilab.machine_tag set tags = #{tags}, update_time = #{updateTime} where machine_id = #{machineId}")
    void updateMachineTagById(MachineTag tag);

    @Delete("delete from minilab.machine_tag where machine_id = #{id}")
    void deleteTagByMachineId(Integer id);
}
