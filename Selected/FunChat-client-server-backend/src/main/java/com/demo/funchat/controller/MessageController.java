package com.demo.funchat.controller;

import com.demo.funchat.entity.MessageEntity;
import com.demo.funchat.service.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * Send Message
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestParam Long senderId,
            @RequestParam Long groupId,
            @RequestParam String content
    ) {
        MessageEntity message = messageService.sendMessage(senderId, groupId, content);

        messagingTemplate.convertAndSend("/topic/group." + groupId, message);

        return ResponseEntity.ok(message);
    }

    /**
     * Get all messages of group
     */
    @GetMapping("/group/{groupId}")
    public List<MessageEntity> getMessages(@PathVariable Long groupId) {
        return messageService.getMessagesByGroupId(groupId);
    }

    /**
     * Add Emotes
     */
    @PostMapping("/emote")
    public ResponseEntity<?> addEmote(
            @RequestParam Long id,
            @RequestParam Integer emote
    ) {
        MessageEntity updatedMessage = messageService.addEmote(id, emote);

        // broadcast the updated message to everyone in that group
        messagingTemplate.convertAndSend(
                "/topic/group." + updatedMessage.getGroupId(),
                updatedMessage
        );

        return ResponseEntity.ok(updatedMessage);
    }
}
