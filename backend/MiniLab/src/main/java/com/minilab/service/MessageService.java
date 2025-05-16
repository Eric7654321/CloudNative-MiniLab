package com.minilab.service;

import com.minilab.pojo.entity.Message;

import java.util.List;

public interface MessageService {
    void saveMessage(Message message);

    List<Message> getMsgByGroupId(String groupId);
}
