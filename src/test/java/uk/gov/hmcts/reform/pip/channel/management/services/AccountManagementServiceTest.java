package uk.gov.hmcts.reform.pip.channel.management.services;

import com.azure.core.http.ContentType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import nl.altindag.log.LogCaptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.hmcts.reform.pip.channel.management.Application;
import uk.gov.hmcts.reform.pip.channel.management.config.AzureBlobTestConfiguration;
import uk.gov.hmcts.reform.pip.channel.management.config.WebClientTestConfiguration;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest(classes = {Application.class, WebClientTestConfiguration.class, AzureBlobTestConfiguration.class})
@ActiveProfiles(profiles = "test")
class AccountManagementServiceTest {

    private static MockWebServer mockAccountManagementEndpoint;
    private final ObjectWriter ow = new ObjectMapper().findAndRegisterModules().writer().withDefaultPrettyPrinter();
    private static final String BAD_MAP_ERROR = "Map does not match expected result.";

    @Autowired
    WebClient webClient;

    @Autowired
    AccountManagementService accountManagementService;

    @BeforeEach
    void setup() throws IOException {
        mockAccountManagementEndpoint = new MockWebServer();
        mockAccountManagementEndpoint.start(6969);
    }

    @AfterEach
    void teardown() throws IOException {
        mockAccountManagementEndpoint.shutdown();
    }

    @Test
    void testGetEmails() throws IOException {
        Map<String, Optional<String>> testMap = new ConcurrentHashMap<>();
        List<String> emailList = new ArrayList<>();
        emailList.add("test123");
        testMap.put("test123", Optional.of("test@email.com"));

        mockAccountManagementEndpoint.enqueue(new MockResponse().addHeader(
            "Content-Type",
            ContentType.APPLICATION_JSON
        ).setBody(ow.writeValueAsString(testMap)));

        Map<String, Optional<String>> returnedMap = accountManagementService.getEmails(emailList);
        assertEquals(
            testMap,
            returnedMap,
            BAD_MAP_ERROR
        );

    }

    @Test
    void testEmptyList() throws IOException {
        Map<String, Optional<String>> testMap = new ConcurrentHashMap<>();
        mockAccountManagementEndpoint.enqueue(new MockResponse().addHeader(
            "Content-Type",
            ContentType.APPLICATION_JSON
        ).setBody(ow.writeValueAsString(testMap)));

        List<String> emailList = new ArrayList<>();
        Map<String, Optional<String>> returnedMap = accountManagementService.getEmails(emailList);
        assertEquals(
            testMap,
            returnedMap,
            BAD_MAP_ERROR
        );

    }

    @Test
    void testException() throws IOException {

        List<String> emailList = new ArrayList<>();
        mockAccountManagementEndpoint.enqueue(new MockResponse().setResponseCode(404));
        try (LogCaptor logCaptor = LogCaptor.forClass(AccountManagementService.class)) {
            assertTrue(accountManagementService.getEmails(emailList).isEmpty(),
                       "should be empty when an exception is thrown");
            assertTrue(logCaptor.getErrorLogs().get(0)
                           .contains("Request to Account Management to get account e-mails failed with error"),
                       "Messages do not match");
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Test
    void testIsAuthorised() {
        mockAccountManagementEndpoint.enqueue(new MockResponse().setBody("true")
                                                  .addHeader("Content-Type", "application/json"));

        boolean isAuthorised =
            accountManagementService.getIsAuthorised(UUID.randomUUID(),
                                                     ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.PUBLIC);

        assertTrue(isAuthorised, "Authorised has not been returned from the server");
    }

    @Test
    void testIsAuthorisedError() {
        mockAccountManagementEndpoint.enqueue(new MockResponse().setResponseCode(BAD_REQUEST.value()));

        boolean isAuthorised =
            accountManagementService.getIsAuthorised(UUID.randomUUID(),
                                                     ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.PUBLIC);

        assertFalse(isAuthorised, "Not authorised has not been returned from the server");
    }
}
