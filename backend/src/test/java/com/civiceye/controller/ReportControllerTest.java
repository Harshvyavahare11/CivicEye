package com.civiceye.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listReportsShouldReturnSeedReports() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    void getStatisticsShouldReturnStatusCounts() throws Exception {
        mockMvc.perform(get("/api/reports/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReports").isNumber())
                .andExpect(jsonPath("$.byStatus").isMap());
    }

    @Test
    void analyzeShouldRejectEmptyImage() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image", "empty.jpg", "image/jpeg", new byte[0]
        );

        mockMvc.perform(multipart("/api/reports/analyze")
                        .file(emptyFile)
                        .param("description", "Testing empty file upload"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("empty")));
    }

    @Test
    void analyzeShouldRejectUnsupportedContentType() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "image", "document.pdf", "application/pdf", "Fake PDF data".getBytes()
        );

        mockMvc.perform(multipart("/api/reports/analyze")
                        .file(pdfFile)
                        .param("description", "Testing PDF upload"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Only JPEG, PNG, and WebP")));
    }

    @Test
    void createReportShouldReturnCreated() throws Exception {
        String jsonPayload = """
                {
                    "title": "Broken street lamp near school",
                    "category": "Streetlight",
                    "description": "Flickering and burnt out lamp",
                    "location": "Near City High School Gate",
                    "complaintText": "Streetlight near the school gate is completely dark. Please repair.",
                    "severity": "Medium",
                    "status": "Ready for Submission"
                }
                """;

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Broken street lamp near school"))
                .andExpect(jsonPath("$.category").value("Streetlight"));
    }

    @Test
    void sendComplaintShouldDeliverViaMessagingAdapter() throws Exception {
        String sendBody = """
                {
                    "recipientPhone": "+15559876543",
                    "customNote": "Testing automated complaint dispatch"
                }
                """;

        mockMvc.perform(post("/api/reports/CE-2026-1002/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sendBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value("CE-2026-1002"))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.communicationStatus").value(org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.is("Sent"),
                        org.hamcrest.Matchers.is("Delivered")
                )))
                .andExpect(jsonPath("$.providerMessageId").isNotEmpty());
    }

    @Test
    void simulateInboundWebhookStatusKeyword() throws Exception {
        String simJson = """
                {
                    "from": "+15551234567",
                    "body": "STATUS CE-2026-1001"
                }
                """;

        mockMvc.perform(post("/api/webhooks/messaging/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(simJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value("CE-2026-1001"))
                .andExpect(jsonPath("$.action").value("STATUS_QUERY"))
                .andExpect(jsonPath("$.replyText").value(org.hamcrest.Matchers.containsString("CivicEye Report Status")));
    }

    @Test
    void simulateInboundWebhookFixedKeywordResolvesReport() throws Exception {
        String simJson = """
                {
                    "from": "+15551234567",
                    "body": "FIXED CE-2026-1001"
                }
                """;

        mockMvc.perform(post("/api/webhooks/messaging/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(simJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value("CE-2026-1001"))
                .andExpect(jsonPath("$.action").value("MARKED_FIXED"))
                .andExpect(jsonPath("$.newStatus").value("Awaiting Citizen Confirmation"));
    }

    @Test
    void simulateInboundWebhookNotFixedKeywordEscalatesReport() throws Exception {
        String simJson = """
                {
                    "from": "+15551234567",
                    "body": "NOT_FIXED CE-2026-1001"
                }
                """;

        mockMvc.perform(post("/api/webhooks/messaging/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(simJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value("CE-2026-1001"))
                .andExpect(jsonPath("$.action").value("ESCALATED"))
                .andExpect(jsonPath("$.newStatus").value("Escalated"));
    }

    @Test
    void simulateInboundWebhookHelpKeyword() throws Exception {
        String simJson = """
                {
                    "from": "+15551234567",
                    "body": "HELP"
                }
                """;

        mockMvc.perform(post("/api/webhooks/messaging/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(simJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("HELP"))
                .andExpect(jsonPath("$.replyText").value(org.hamcrest.Matchers.containsString("CivicEye SMS/WhatsApp Bot Commands")));
    }
}
