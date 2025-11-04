package com.demo.funchat.controller;

import com.demo.funchat.entity.MessageEntity;
import com.demo.funchat.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestParam Long senderId,
            @RequestParam Long groupId,
            @RequestParam String content
    ) {
        MessageEntity message = messageService.sendMessage(groupId, senderId, content);
        return ResponseEntity.ok(message);
    }
}
