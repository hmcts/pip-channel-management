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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {Application.class, WebClientTestConfiguration.class, AzureBlobTestConfiguration.class})
@ActiveProfiles(profiles = "test")
class AccountManagementServiceTest {

    private static MockWebServer mockAccountManagementGetEmailsEndpoint;
    private final ObjectWriter ow = new ObjectMapper().findAndRegisterModules().writer().withDefaultPrettyPrinter();
    private static final String BAD_MAP_ERROR = "Map does not match expected result.";

    @Autowired
    WebClient webClient;

    @Autowired
    AccountManagementService accountManagementService;

    @BeforeEach
    void setup() throws IOException {
        mockAccountManagementGetEmailsEndpoint = new MockWebServer();
        mockAccountManagementGetEmailsEndpoint.start(6969);
    }

    @AfterEach
    void teardown() throws IOException {
        mockAccountManagementGetEmailsEndpoint.shutdown();
    }

    @Test
    void testGetEmails() throws IOException {
        Map<String, Optional<String>> testMap = new ConcurrentHashMap<>();
        List<String> emailList = new ArrayList<>();
        emailList.add("test123");
        testMap.put("test123", Optional.of("test@email.com"));

        mockAccountManagementGetEmailsEndpoint.enqueue(new MockResponse().addHeader(
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
        mockAccountManagementGetEmailsEndpoint.enqueue(new MockResponse().addHeader(
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
        mockAccountManagementGetEmailsEndpoint.enqueue(new MockResponse().setResponseCode(404));
        try (LogCaptor logCaptor = LogCaptor.forClass(AccountManagementService.class)) {
            assertTrue(accountManagementService.getEmails(emailList).isEmpty(),
                       "should be empty when an exception is thrown");
            assertTrue(logCaptor.getErrorLogs().get(0).contains("Account management request failed to get emails"),
                       "Messages do not match");
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }
}
