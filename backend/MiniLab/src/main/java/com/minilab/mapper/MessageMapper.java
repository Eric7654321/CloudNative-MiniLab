package com.minilab.mapper;

import com.minilab.pojo.entity.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MessageMapper {
    @Insert("insert into minilab.message (task_id, description, `group`, status, update_time) VALUES (#{taskId}, #{description}, #{group}, #{status}, #{updateTime})")
    void saveMessage(Message message);

    @Select("select * from minilab.message where `group` = #{groupId} order by update_time desc")
    List<Message> getMessageByGroupId(String groupId);
}
