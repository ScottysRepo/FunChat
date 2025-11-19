package com.demo.funchat.service;

import com.demo.funchat.entity.MessageEntity;
import com.demo.funchat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public MessageEntity addEmote(Long messageId, Integer emote) {
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        Map<Integer, Integer> map = message.getEmotes();
        if (map == null) map = new HashMap<>();
        map.put(emote, map.getOrDefault(emote, 0) + 1);
        message.setEmotes(map);

        return messageRepository.save(message);
    }
}
