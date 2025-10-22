package com.demo.funchat.controller;

import com.demo.funchat.model.Message;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    Message test = new Message("Len", "Hello");
    private List<Message> messages = new ArrayList<>();


    @GetMapping
    public List<Message> getMessages() {
        messages.add(test);
        return messages;
    }

    @PostMapping
    public Message addMessage(@RequestBody Message message) {
        messages.add(message);
        return message;
    }
}

