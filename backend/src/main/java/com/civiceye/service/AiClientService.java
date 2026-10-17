package com.civiceye.service;

import com.civiceye.dto.AiHealthResponse;
import com.civiceye.dto.AnalysisResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Service managing communication with the Python FastAPI AI Engine.
 */
@Service
public class AiClientService {

    private static final Logger log = LoggerFactory.getLogger(AiClientService.class);

    private final RestTemplate restTemplate;

    @Value("${civiceye.ai-engine.base-url:http://localhost:8000}")
    private String aiEngineBaseUrl;

    public AiClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Checks if the Python AI-engine is alive and reports its status.
     */
    public AiHealthResponse checkAiHealth() {
        String url = aiEngineBaseUrl + "/health";
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map body = response.getBody();
                boolean keyConfigured = Boolean.TRUE.equals(body.get("gemma_api_key_configured"));
                String model = (String) body.get("model");
                return AiHealthResponse.connected(keyConfigured, model);
            }
            return AiHealthResponse.disconnected(aiEngineBaseUrl, "HTTP status " + response.getStatusCode());
        } catch (ResourceAccessException ex) {
            log.warn("AI Engine unreachable at {}: {}", url, ex.getMessage());
            return AiHealthResponse.disconnected(aiEngineBaseUrl, "Connection refused or timed out");
        } catch (Exception ex) {
            log.warn("AI Engine health check error: {}", ex.getMessage());
            return AiHealthResponse.disconnected(aiEngineBaseUrl, ex.getMessage());
        }
    }

    /**
     * Forwards an uploaded image and context to the Python FastAPI service.
     */
    public AnalysisResultDto analyzeImage(
            MultipartFile image,
            String description,
            String location,
            boolean demoMode
    ) throws IOException {
        String url = aiEngineBaseUrl + "/analyze";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // Add file as a named ByteArrayResource
        ByteArrayResource imageResource = new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename() != null ? image.getOriginalFilename() : "image.jpg";
            }
        };

        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType(
                image.getContentType() != null ? image.getContentType() : "image/jpeg"
        ));
        HttpEntity<ByteArrayResource> fileEntity = new HttpEntity<>(imageResource, fileHeaders);
        body.add("image", fileEntity);

        if (description != null && !description.isBlank()) {
            body.add("description", description.trim());
        }
        if (location != null && !location.isBlank()) {
            body.add("location", location.trim());
        }
        body.add("demo_mode", String.valueOf(demoMode));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        log.info("Forwarding analysis request to AI engine at {}. DemoMode={}", url, demoMode);
        ResponseEntity<AnalysisResultDto> response = restTemplate.postForEntity(url, requestEntity, AnalysisResultDto.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }

        throw new RuntimeException("AI Engine returned unexpected status: " + response.getStatusCode());
    }

    public String getAiEngineBaseUrl() {
        return aiEngineBaseUrl;
    }
}
