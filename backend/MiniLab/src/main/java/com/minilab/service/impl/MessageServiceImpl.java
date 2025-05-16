package com.minilab.service.impl;

import com.minilab.mapper.MessageMapper;
import com.minilab.mapper.TaskMapper;
import com.minilab.pojo.entity.Message;
import com.minilab.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Autowired
    MessageMapper messageMapper;
    @Autowired
    TaskMapper taskMapper;

    @Override
    public void saveMessage(Message message) {
        //根據回報狀態更新任務狀態
        Integer taskId = message.getTaskId();
        if(message.getStatus() == 0){
            taskMapper.solveTask(taskId);
        }
        messageMapper.saveMessage(message);
    }

    @Override
    public List<Message> getMsgByGroupId(String groupId) {
        return messageMapper.getMessageByGroupId(groupId);
    }
}
