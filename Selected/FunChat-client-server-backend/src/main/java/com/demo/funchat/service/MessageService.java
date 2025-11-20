package com.demo.funchat.service;

import com.demo.funchat.entity.MessageEntity;
import com.demo.funchat.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public MessageEntity sendMessage(Long senderId, Long groupId, String content) {

        MessageEntity msg = new MessageEntity();
        msg.setSenderId(senderId);
        msg.setGroupId(groupId);
        msg.setContent(content);
        msg.setSentAt(LocalDateTime.now());
        msg.setEmote(null); //because no intial emote reaction
        return messageRepository.save(msg);
    }

    public List<MessageEntity> getMessagesByGroupId(Long groupId) {
        return messageRepository.findByGroupIdOrderBySentAtAsc(groupId);
    }


    public MessageEntity addEmote(Long messageId, Integer emote) {

        MessageEntity msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException(
                        "Message not found with id " + messageId
                ));

        msg.setEmote(emote);

        return messageRepository.save(msg);
    }
}
