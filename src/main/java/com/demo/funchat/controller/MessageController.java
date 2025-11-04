package com.demo.funchat.controller;

import com.demo.funchat.entity.MessageEntity;
import com.demo.funchat.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Send Message
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestParam Long senderId,
            @RequestParam Long groupId,
            @RequestParam String content
    ) {
        MessageEntity message = messageService.sendMessage(groupId, senderId, content);
        return ResponseEntity.ok(message);
    }

    /**
     * Get all messages of group
     */
    @GetMapping("/group/{groupId}")
    public List<MessageEntity> getMessages(@PathVariable Long groupId) {
        return messageService.getMessagesByGroupId(groupId);
    }
}
