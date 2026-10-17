package com.civiceye;

import com.civiceye.dto.*;
import com.civiceye.entity.ReportCategory;
import com.civiceye.entity.ReportStatus;
import com.civiceye.service.CivicReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CivicReportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CivicReportService civicReportService;

    @Test
    @DisplayName("1. Creating a draft report starts in DRAFT status and records initial status history")
    void shouldCreateDraftReportAndRecordInitialHistory() throws Exception {
        String draftPayload = """
                {
                    "title": "Severe road crater on MG Road",
                    "description": "Deep depression in fast lane near pillar 42",
                    "category": "POTHOLE",
                    "severity": "HIGH",
                    "locationText": "MG Road near Metro Pillar 42",
                    "latitude": 18.520430,
                    "longitude": 73.856743,
                    "suggestedDepartment": "Roads & Bridges Department",
                    "recipientPhone": "+15551234567"
                }
                """;

        String responseBody = mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(draftPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.reportNumber").isNotEmpty())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.category").value("POTHOLE"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.latitude").value(18.520430))
                .andExpect(jsonPath("$.statusHistoryCount").value(1))
                .andReturn().getResponse().getContentAsString();

        // Verify status history endpoint
        String reportNumber = com.jayway.jsonpath.JsonPath.read(responseBody, "$.reportNumber");
        mockMvc.perform(get("/api/reports/" + reportNumber + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].newStatus").value("DRAFT"))
                .andExpect(jsonPath("$[0].previousStatus").doesNotExist());
    }

    @Test
    @DisplayName("2. Retrieving a report by ID and by human-readable reportNumber")
    void shouldRetrieveReportByIdAndReportNumber() throws Exception {
        CreateDraftReportRequest req = new CreateDraftReportRequest(
                "Garbage heap on Market Road",
                "Overflowing bins blocking the pedestrian footpath",
                null,
                "GARBAGE",
                "MEDIUM",
                "Market Road Sector 2",
                null,
                BigDecimal.valueOf(18.530000),
                BigDecimal.valueOf(73.850000),
                "Solid Waste Department",
                "+15551234567"
        );
        ReportResponse created = civicReportService.createDraftReport(req);

        // Retrieve by UUID
        mockMvc.perform(get("/api/reports/" + created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportNumber").value(created.reportNumber()))
                .andExpect(jsonPath("$.category").value("GARBAGE"));

        // Retrieve by reportNumber
        mockMvc.perform(get("/api/reports/" + created.reportNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.id().toString()))
                .andExpect(jsonPath("$.title").value("Garbage heap on Market Road"));
    }

    @Test
    @DisplayName("3. Filtering and paginating reports with pagination metadata")
    void shouldFilterAndPaginateReports() throws Exception {
        civicReportService.createDraftReport(new CreateDraftReportRequest(
                "Open drain on North Avenue",
                "Damaged concrete slab over storm drain",
                null,
                "OPEN_DRAIN",
                "HIGH",
                "North Avenue Ward 5",
                null,
                BigDecimal.valueOf(18.510000),
                BigDecimal.valueOf(73.820000),
                "Stormwater Management",
                "+15551234567"
        ));

        mockMvc.perform(get("/api/reports?category=OPEN_DRAIN&page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[0].category").value("OPEN_DRAIN"));
    }

    @Test
    @DisplayName("4. Saving multiple AI analyses for one report preserves history without overwriting")
    void shouldSaveMultipleAiAnalysesWithoutOverwriting() throws Exception {
        ReportResponse report = civicReportService.createDraftReport(new CreateDraftReportRequest(
                "Waterlogged junction after rain",
                "Knee deep water accumulated at crossroad",
                null,
                "WATERLOGGING",
                "HIGH",
                "Swargate Junction",
                null,
                null,
                null,
                "Drainage Maintenance",
                "+15551234567"
        ));

        // First AI analysis
        String analysis1 = """
                {
                    "modelName": "gemma-4-26b-a4b-it",
                    "promptVersion": "v1.0",
                    "observations": ["Water level covers road median", "Sediment accumulation"],
                    "severityReason": "Vehicular engine hydro-locking danger",
                    "uncertainties": ["Drain depth below water surface is invisible"],
                    "suggestedDepartment": "Stormwater Drainage Dept",
                    "nextSteps": ["Deploy de-watering pumps"]
                }
                """;

        mockMvc.perform(post("/api/reports/" + report.id() + "/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(analysis1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.modelName").value("gemma-4-26b-a4b-it"))
                .andExpect(jsonPath("$.observations.length()").value(2));

        // Second refined AI analysis
        String analysis2 = """
                {
                    "modelName": "gemma-4-26b-a4b-it",
                    "promptVersion": "v1.1",
                    "observations": ["Water receded by 4 inches", "Exposed clogged grate"],
                    "severityReason": "Clogged catchment pit prevents gravity outflow",
                    "uncertainties": ["Sub-surface pipe blockage unknown"],
                    "suggestedDepartment": "Stormwater Drainage Dept",
                    "nextSteps": ["Clear surface debris from storm grate"]
                }
                """;

        mockMvc.perform(post("/api/reports/" + report.id() + "/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(analysis2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.promptVersion").value("v1.1"));

        // Fetch history and assert both analyses exist
        mockMvc.perform(get("/api/reports/" + report.id() + "/analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].promptVersion").value("v1.1"))
                .andExpect(jsonPath("$[1].promptVersion").value("v1.0"));
    }

    @Test
    @DisplayName("5. Status transitions record history and update lifecycle state")
    void shouldTrackStatusTransitionsAccurately() throws Exception {
        ReportResponse report = civicReportService.createDraftReport(new CreateDraftReportRequest(
                "Damaged pedestrian curb",
                "Broken curb tiles presenting tripping hazard",
                null,
                "OTHER",
                "LOW",
                "FC Road Footpath",
                null,
                null,
                null,
                "Roads Dept",
                "+15551234567"
        ));

        // DRAFT -> READY_TO_SUBMIT
        String transition1 = """
                {
                    "newStatus": "READY_TO_SUBMIT",
                    "notes": "Citizen verified AI complaint text and confirmed GPS pin",
                    "changedBy": "CITIZEN"
                }
                """;
        mockMvc.perform(patch("/api/reports/" + report.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transition1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY_TO_SUBMIT"));

        // READY_TO_SUBMIT -> SUBMITTED
        String transition2 = """
                {
                    "newStatus": "SUBMITTED",
                    "notes": "Grievance dispatched to ward authority via messaging gateway",
                    "changedBy": "GATEWAY"
                }
                """;
        mockMvc.perform(patch("/api/reports/" + report.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transition2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.submittedAt").isNotEmpty());

        // Check complete history (Initial DRAFT + 2 transitions = 3 history entries)
        mockMvc.perform(get("/api/reports/" + report.id() + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].newStatus").value("SUBMITTED"))
                .andExpect(jsonPath("$[0].previousStatus").value("READY_TO_SUBMIT"))
                .andExpect(jsonPath("$[1].newStatus").value("READY_TO_SUBMIT"))
                .andExpect(jsonPath("$[1].previousStatus").value("DRAFT"));
    }

    @Test
    @DisplayName("6. Rejecting invalid status transitions according to state machine rules")
    void shouldRejectInvalidStatusTransitions() throws Exception {
        ReportResponse report = civicReportService.createDraftReport(new CreateDraftReportRequest(
                "Uncollected garbage bags",
                "Garbage piled at street corner",
                null,
                "GARBAGE",
                "LOW",
                "Lane 4 corner",
                null,
                null,
                null,
                "Solid Waste",
                "+15551234567"
        ));

        // Invalid: DRAFT directly to RESOLVED without ever being submitted or worked on
        String invalidTransition = """
                {
                    "newStatus": "RESOLVED",
                    "notes": "Premature attempt to resolve an unverified draft",
                    "changedBy": "TESTER"
                }
                """;

        mockMvc.perform(patch("/api/reports/" + report.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidTransition))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid status transition from DRAFT to RESOLVED")));
    }

    @Test
    @DisplayName("7. Validating coordinates, file size, and evidence metadata")
    void shouldValidateCoordinatesAndEvidenceMetadata() throws Exception {
        // Invalid latitude (> 90.0)
        String invalidCoordPayload = """
                {
                    "title": "Invalid Coordinate Test",
                    "description": "Latitude exceeds 90 degrees",
                    "category": "OTHER",
                    "latitude": 98.765432,
                    "longitude": 73.856743
                }
                """;

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidCoordPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Latitude must be <= 90.0")));

        // Valid report
        ReportResponse report = civicReportService.createDraftReport(new CreateDraftReportRequest(
                "Evidence validation report",
                "Testing photo evidence upload",
                null,
                "OTHER",
                "LOW",
                null,
                null,
                null,
                null,
                null,
                null
        ));

        // Empty file rejection
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);
        mockMvc.perform(multipart("/api/reports/" + report.id() + "/evidence").file(emptyFile))
                .andExpect(status().isBadRequest());

        // Non-image rejection
        MockMultipartFile textFile = new MockMultipartFile("file", "doc.txt", "text/plain", "Hello world".getBytes());
        mockMvc.perform(multipart("/api/reports/" + report.id() + "/evidence").file(textFile))
                .andExpect(status().isUnsupportedMediaType());

        // Valid image upload
        MockMultipartFile validImage = new MockMultipartFile("file", "pothole.png", "image/png", new byte[]{1, 2, 3, 4});
        mockMvc.perform(multipart("/api/reports/" + report.id() + "/evidence").file(validImage))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalFilename").value("pothole.png"))
                .andExpect(jsonPath("$.contentType").value("image/png"))
                .andExpect(jsonPath("$.fileSizeBytes").value(4))
                .andExpect(jsonPath("$.storagePath").value(org.hamcrest.Matchers.startsWith("evidence/")));
    }

    @Test
    @DisplayName("8. Transaction rollback when an operation fails during transition")
    void shouldRollbackTransactionOnFailure() {
        ReportResponse report = civicReportService.createDraftReport(new CreateDraftReportRequest(
                "Rollback Test Report",
                "Testing transaction safety",
                null,
                "OTHER",
                "LOW",
                null,
                null,
                null,
                null,
                null,
                null
        ));

        // Attempting an illegal transition throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            civicReportService.changeReportStatus(
                    report.id().toString(),
                    new ChangeStatusRequest("RESOLVED", "Should fail and rollback", "TESTER")
            );
        });

        // Verify status remained DRAFT and no corrupt status history was inserted
        ReportResponse current = civicReportService.getReportDetails(report.id().toString());
        assertThat(current.status()).isEqualTo("DRAFT");
        assertThat(current.statusHistoryCount()).isEqualTo(1); // Only initial creation history
    }
}
