package com.minilab.mapper;

import com.minilab.pojo.entity.Machine;
import com.minilab.pojo.vo.EmpVO;
import com.minilab.pojo.vo.MachineVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MachineMapper {

    @Select("select * from minilab.machine where `group` = #{groupId}")
    List<MachineVO> getMachineByGroupId(String groupId);

    @Select("select tags from minilab.machine_tag where machine_id = #{id}")
    String setTagsByUserId(Integer id);
}
