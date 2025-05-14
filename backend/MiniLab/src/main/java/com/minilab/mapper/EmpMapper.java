package com.minilab.mapper;

import com.minilab.pojo.entity.Emp;
import com.minilab.pojo.vo.EmpVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper
public interface EmpMapper {
    @Select("select * from minilab.emp where username = #{username} and password = #{password}")
    Emp getByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    @Select("select * from minilab.emp where `group` = #{groupId}")
    List<EmpVO> getEmpByGroupId(String groupId);

    @Select("select tags from minilab.emp_tag where emp_id = #{id}")
    String setTagsByUserId(Integer id);
}
