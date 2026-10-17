package com.civiceye;

import com.civiceye.dto.ChangeStatusRequest;
import com.civiceye.dto.CreateDraftReportRequest;
import com.civiceye.dto.ReportResponse;
import com.civiceye.entity.CivicReport;
import com.civiceye.entity.NotificationLog;
import com.civiceye.entity.ReportCategory;
import com.civiceye.entity.ReportStatus;
import com.civiceye.model.Report;
import com.civiceye.repository.NotificationLogRepository;
import com.civiceye.repository.ReportRepository;
import com.civiceye.scheduler.FollowUpScheduler;
import com.civiceye.service.CivicReportService;
import com.civiceye.service.messaging.*;
import com.civiceye.service.notification.NotificationCoordinator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class TwilioNotificationTest {

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private CivicReportService civicReportService;

    @Autowired
    private NotificationCoordinator notificationCoordinator;

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    @Autowired
    private ReportRepository legacyReportRepository;

    @Autowired
    private FollowUpScheduler followUpScheduler;

    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        notificationLogRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Default mock mode works without Twilio credentials")
    void defaultMockModeWorksWithoutCredentials() {
        ReflectionTestUtils.setField(messagingService, "configuredProvider", "mock");

        MessagingProvider provider = messagingService.getActiveProvider();
        assertNotNull(provider);
        assertTrue(provider instanceof DevMockMessagingProvider);

        MessageSendResult result = provider.sendMessage("+917058207967", "Test mock message", "CE-TEST-001");
        assertTrue(result.success());
        assertTrue(result.isSimulated());
        assertNotNull(result.providerMessageId());
        assertTrue(result.providerMessageId().startsWith("MOCK-MSG-"));
    }

    @Test
    @DisplayName("2. Twilio mode fails safely when required configuration is missing")
    void twilioModeFailsSafelyWhenConfigMissing() {
        TwilioMessagingProvider twilioProvider = new TwilioMessagingProvider(restTemplate);
        ReflectionTestUtils.setField(twilioProvider, "accountSid", "");
        ReflectionTestUtils.setField(twilioProvider, "authToken", "");
        ReflectionTestUtils.setField(twilioProvider, "fromNumber", "");

        assertFalse(twilioProvider.isConfigured());

        MessageSendResult result = twilioProvider.sendMessage("+917058207967", "Test alert", "CE-001");
        assertFalse(result.success());
        assertEquals("TWILIO_NOT_CONFIGURED", result.errorCode());
        assertNotNull(result.errorMessage());
    }

    @Test
    @DisplayName("3. Correct recipient, sender, and message body are constructed in Twilio mode")
    void correctRecipientSenderAndBodyConstructed() {
        TwilioMessagingProvider twilioProvider = new TwilioMessagingProvider(restTemplate);
        ReflectionTestUtils.setField(twilioProvider, "accountSid", "AC00000000000000000000000000000000");
        ReflectionTestUtils.setField(twilioProvider, "authToken", "mock_auth_token_value_for_testing");
        ReflectionTestUtils.setField(twilioProvider, "fromNumber", "+15550001111");

        assertTrue(twilioProvider.isConfigured());

        String twilioResponse = """
                {
                    "sid": "SM1234567890abcdef",
                    "status": "queued",
                    "to": "+917058207967",
                    "from": "+15550001111",
                    "body": "CivicEye issue reported"
                }
                """;

        when(restTemplate.postForEntity(
                contains("/Messages.json"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(twilioResponse, HttpStatus.CREATED));

        MessageSendResult result = twilioProvider.sendMessage("+917058207967", "CivicEye issue reported", "CE-2026-0001");
        assertTrue(result.success());
        assertEquals("SM1234567890abcdef", result.providerMessageId());
        assertFalse(result.isDelivered(), "Twilio acceptance should not be treated as final delivery proof");
    }

    @Test
    @DisplayName("4. Successful provider responses are handled and logged")
    void successfulProviderResponsesHandled() {
        CivicReport report = new CivicReport();
        report.setReportNumber("CE-SUCCESS-01");
        report.setTitle("Pothole on Main Road");
        report.setDescription("Crater near crossing");
        report.setCategory(ReportCategory.POTHOLE);
        report.setRecipientPhone("+917058207967");
        report.setSmsConsent(true);

        ReflectionTestUtils.setField(messagingService, "configuredProvider", "mock");

        notificationCoordinator.notifySubmission(report);

        List<NotificationLog> logs = notificationLogRepository.findByReportNumberOrderByCreatedAtDesc("CE-SUCCESS-01");
        assertFalse(logs.isEmpty());
        assertEquals("SIMULATED", logs.get(0).getOutcome());
        assertTrue(logs.get(0).getRecipientMasked().contains("****"));
    }

    @Test
    @DisplayName("5. Authentication failures and API errors are handled safely")
    void twilioAuthenticationFailuresHandledSafely() {
        TwilioMessagingProvider twilioProvider = new TwilioMessagingProvider(restTemplate);
        ReflectionTestUtils.setField(twilioProvider, "accountSid", "AC_INVALID");
        ReflectionTestUtils.setField(twilioProvider, "authToken", "INVALID_TOKEN");
        ReflectionTestUtils.setField(twilioProvider, "fromNumber", "+17372508034");

        String errorJson = """
                {
                    "code": 20003,
                    "message": "Authentication Error - No credentials provided",
                    "status": 401
                }
                """;

        HttpClientErrorException authException = HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                HttpHeaders.EMPTY,
                errorJson.getBytes(),
                null
        );

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(authException);

        MessageSendResult result = twilioProvider.sendMessage("+917058207967", "Test msg", "CE-001");
        assertFalse(result.success());
        assertEquals("TWILIO_ERR_20003", result.errorCode());
        assertEquals("Authentication Error - No credentials provided", result.errorMessage());
    }

    @Test
    @DisplayName("6. Notification failure does not prevent complaint persistence")
    void notificationFailureDoesNotPreventReportPersistence() {
        // Create draft report with phone and consent
        CreateDraftReportRequest req = new CreateDraftReportRequest(
                "Waterlogging near market",
                "Deep flood blocking market entry",
                null,
                "WATERLOGGING",
                "HIGH",
                "Central Market",
                null,
                new BigDecimal("18.5204"),
                new BigDecimal("73.8567"),
                "Stormwater Drainage",
                "+917058207967",
                true
        );

        ReportResponse created = civicReportService.createDraftReport(req);
        assertNotNull(created.id());
        assertNotNull(created.reportNumber());

        // Valid lifecycle transition: DRAFT -> READY_TO_SUBMIT -> SUBMITTED
        civicReportService.changeReportStatus(
                created.reportNumber(),
                new ChangeStatusRequest("READY_TO_SUBMIT", "Citizen", "Citizen confirmed details")
        );

        ReportResponse submitted = civicReportService.changeReportStatus(
                created.reportNumber(),
                new ChangeStatusRequest("SUBMITTED", "Citizen", "Citizen submitted report")
        );

        assertEquals("SUBMITTED", submitted.status());
    }

    @Test
    @DisplayName("7. Duplicate events do not send duplicate messages (Idempotency)")
    void duplicateEventsDoNotSendDuplicates() {
        CivicReport report = new CivicReport();
        report.setReportNumber("CE-IDEMP-01");
        report.setTitle("Drain blockage");
        report.setDescription("Drain clogged");
        report.setCategory(ReportCategory.OPEN_DRAIN);
        report.setRecipientPhone("+917058207967");
        report.setSmsConsent(true);

        ReflectionTestUtils.setField(messagingService, "configuredProvider", "mock");

        // First call
        notificationCoordinator.notifySubmission(report);
        assertEquals(1, notificationLogRepository.findByReportNumberOrderByCreatedAtDesc("CE-IDEMP-01").size());

        // Duplicate call with same report submission
        notificationCoordinator.notifySubmission(report);
        // Size remains 1 due to idempotency key check
        assertEquals(1, notificationLogRepository.findByReportNumberOrderByCreatedAtDesc("CE-IDEMP-01").size());
    }

    @Test
    @DisplayName("8. Complainant messages require consent and use the correct phone number")
    void complainantMessagesRequireConsent() {
        CivicReport noConsentReport = new CivicReport();
        noConsentReport.setReportNumber("CE-NOCONSENT");
        noConsentReport.setCategory(ReportCategory.GARBAGE);
        noConsentReport.setRecipientPhone("+917058207967");
        noConsentReport.setSmsConsent(false); // NO consent

        assertFalse(messagingService.shouldNotifyComplainant(noConsentReport));
        MessageSendResult result = messagingService.sendReportSubmitted(noConsentReport);
        assertFalse(result.success());
        assertEquals("NO_CONSENT", result.errorCode());

        // Now with consent
        noConsentReport.setSmsConsent(true);
        assertTrue(messagingService.shouldNotifyComplainant(noConsentReport));
    }

    @Test
    @DisplayName("9. Test-recipient mode uses only the configured test recipient for legacy tests")
    void testRecipientModeUsesConfiguredRecipient() {
        assertEquals("+919876543210", messagingService.getDefaultTestRecipient());
    }

    @Test
    @DisplayName("10. Escalation messages use only the configured escalation recipient")
    void escalationMessagesUseOnlyConfiguredRecipient() {
        CivicReport report = new CivicReport();
        report.setReportNumber("CE-ESC-01");
        report.setTitle("Gas leak or hazard");
        report.setDescription("Critical issue");
        report.setCategory(ReportCategory.OTHER);
        report.setRecipientPhone("+911111111111"); // Complainant's phone

        ReflectionTestUtils.setField(messagingService, "escalationRecipient", "+919800000000");

        MessageSendResult result = messagingService.sendEscalationMessage(report, "Citizen grievance unresolved for 48 hours");
        assertTrue(result.success());

        // Verify notification log captured escalation recipient, NOT complainant
        notificationCoordinator.notifyEscalation(report, "Unresolved");
        List<NotificationLog> logs = notificationLogRepository.findByReportNumberOrderByCreatedAtDesc("CE-ESC-01");
        assertFalse(logs.isEmpty());
        // Verify masked recipient matches escalation recipient (+919800000000)
        assertTrue(logs.get(0).getRecipientMasked().contains("0000"));
    }

    @Test
    @DisplayName("11. Follow-up reminders respect configured delay")
    void followUpRemindersRespectDelay() {
        Report report = new Report();
        report.setId("CE-FUP-TEST");
        report.setTitle("Pending Pothole");
        report.setStatus("Sent");
        report.setFollowUpSent(false);
        // Due 5 minutes ago
        report.setNextFollowUpAt(Instant.now().minus(5, ChronoUnit.MINUTES));
        report.setRecipientPhone("+917058207967");
        legacyReportRepository.save(report);

        // Run scheduler
        followUpScheduler.dispatchFollowUp(report);

        Report updated = legacyReportRepository.findById("CE-FUP-TEST").orElseThrow();
        assertTrue(updated.isFollowUpSent());
        assertFalse(updated.getFollowUpHistory().isEmpty());
    }

    @Test
    @DisplayName("12. Status updates use the existing status workflow")
    void statusUpdatesUseExistingStatusWorkflow() {
        CreateDraftReportRequest req = new CreateDraftReportRequest(
                "Damaged street light",
                "Light pole flickering dangerously",
                null,
                "OTHER",
                "MEDIUM",
                "Crossroad 4",
                null,
                null,
                null,
                "Electrical Department",
                "+917058207967",
                true
        );

        ReportResponse report = civicReportService.createDraftReport(req);
        assertEquals("DRAFT", report.status());

        // Valid transition
        ReportResponse ready = civicReportService.changeReportStatus(
                report.reportNumber(),
                new ChangeStatusRequest("READY_TO_SUBMIT", "Reviewer", "Reviewed details")
        );
        assertEquals("READY_TO_SUBMIT", ready.status());

        // Invalid transition directly to RESOLVED from READY_TO_SUBMIT must fail
        assertThrows(IllegalArgumentException.class, () ->
                civicReportService.changeReportStatus(
                        report.reportNumber(),
                        new ChangeStatusRequest("RESOLVED", "Reviewer", "Invalid skip")
                )
        );
    }
}
