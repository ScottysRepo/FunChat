package com.demo.funchat.service;

import com.demo.funchat.entity.MessageEntity;
import com.demo.funchat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageEntity sendMessage(Long senderId, Long groupId, String content) {
        MessageEntity message = new MessageEntity();
        message.setSenderId(senderId);
        message.setGroupId(groupId);
        message.setContent(content);

        return messageRepository.save(message);
    }


    public List<MessageEntity> getMessagesByGroupId(Long groupId) {
        return messageRepository.findByGroupIdOrderBySentAtAsc(groupId);
    }
}
