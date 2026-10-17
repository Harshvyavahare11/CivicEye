package com.civiceye.controller;

import com.civiceye.dto.AiHealthResponse;
import com.civiceye.service.AiClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiHealthController {

    private final AiClientService aiClientService;

    public AiHealthController(AiClientService aiClientService) {
        this.aiClientService = aiClientService;
    }

    @GetMapping("/health")
    public ResponseEntity<AiHealthResponse> getAiEngineHealth() {
        AiHealthResponse health = aiClientService.checkAiHealth();
        return ResponseEntity.ok(health);
    }
}
