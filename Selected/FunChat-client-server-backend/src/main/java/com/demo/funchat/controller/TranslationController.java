package com.demo.funchat.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/translate")
public class TranslationController {

    @PostMapping
    public Map<String, String> translate(
            @RequestParam String text,
            @RequestParam String targetLang
    ) {

        String translated = text; 
        Map<String, String> resp = new HashMap<>();
        resp.put("translated", translated);
        return resp;
    }
}
