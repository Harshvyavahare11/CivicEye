package com.civiceye.dto;

public record AiHealthResponse(
        String status,
        String service,
        boolean connected,
        boolean gemmaKeyConfigured,
        String model,
        String message
) {
    public static AiHealthResponse connected(boolean keyConfigured, String model) {
        return new AiHealthResponse(
                "UP",
                "CivicEye AI Engine",
                true,
                keyConfigured,
                model != null ? model : "gemma-4-26b-a4b-it",
                keyConfigured
                        ? "AI Engine is online and Google Gemma 4 is configured."
                        : "AI Engine is online. Gemma API key is missing (Demo Mode available)."
        );
    }

    public static AiHealthResponse disconnected(String targetUrl, String reason) {
        return new AiHealthResponse(
                "DOWN",
                "CivicEye AI Engine",
                false,
                false,
                "unavailable",
                "AI Engine is unreachable at " + targetUrl + ". Reason: " + reason
        );
    }
}
