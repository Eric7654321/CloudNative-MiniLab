package com.minilab.mapper;

import com.minilab.pojo.entity.Emp;
import com.minilab.pojo.entity.EmpTag;
import com.minilab.pojo.vo.EmpVO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@Mapper
public interface EmpMapper {
    @Select("select * from minilab.emp where username = #{username} and password = #{password}")
    Emp getByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    @Select("select * from minilab.emp where `group` = #{groupId}")
    List<EmpVO> getEmpByGroupId(String groupId);

    @Select("select * from minilab.emp where id = #{id}")
    EmpVO getEmpById(Integer id);

    @Select("select tags from minilab.emp_tag where emp_id = #{id}")
    String setTagsByUserId(Integer id);

    @Insert("insert into minilab.emp (username, name, password, usable, `group`, update_time, role) VALUES (#{username}, #{name}, #{password}, #{usable}, #{group}, #{updateTime}, #{role})")
    void insert(Emp emp);

    void updateEmp(Emp emp);

    @Delete("delete from minilab.emp where id = #{id}")
    void deleteEmpById(Emp emp);

    @Select("select * from minilab.emp where username = #{username}")
    Emp getEmpByUsername(String username);
}
