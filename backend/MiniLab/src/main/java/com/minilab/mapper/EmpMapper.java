package com.minilab.mapper;

import com.minilab.pojo.entity.Emp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmpMapper {
    @Select("select * from minilab.emp where username = #{username} and password = #{password}")
    Emp getByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

}
