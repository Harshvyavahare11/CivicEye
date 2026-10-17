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
}
