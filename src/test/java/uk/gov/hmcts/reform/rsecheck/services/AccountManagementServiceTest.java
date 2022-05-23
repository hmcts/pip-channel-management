package uk.gov.hmcts.reform.rsecheck.services;

import com.azure.core.http.ContentType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import nl.altindag.log.LogCaptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.hmcts.reform.demo.Application;
import uk.gov.hmcts.reform.demo.services.AccountManagementService;
import uk.gov.hmcts.reform.rsecheck.config.WebClientConfigurationTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(classes = {Application.class, WebClientConfigurationTest.class})
@ActiveProfiles({"test", "non-async"})
class AccountManagementServiceTest {

    private static MockWebServer mockAccountManagementGetEmailsEndpoint;
    private final ObjectWriter ow = new ObjectMapper().findAndRegisterModules().writer().withDefaultPrettyPrinter();
    private static final String BAD_MAP_ERROR = "Map does not match expected result.";

    @Autowired
    WebClient webClient;

    @Autowired
    AccountManagementService accountManagementService;

    @Test
    void testGetEmails() throws IOException {
        Map<String, Optional<String>> testMap = new ConcurrentHashMap<>();
        List<String> emailList = new ArrayList<>();
        emailList.add("test123");
        testMap.put("test123", Optional.of("test@email.com"));

        mockAccountManagementGetEmailsEndpoint = new MockWebServer();
        mockAccountManagementGetEmailsEndpoint.start(6969);
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
        mockAccountManagementGetEmailsEndpoint.shutdown();
    }

    @Test
    void testEmptyList() throws IOException {
        Map<String, Optional<String>> testMap = new ConcurrentHashMap<>();

        mockAccountManagementGetEmailsEndpoint = new MockWebServer();
        mockAccountManagementGetEmailsEndpoint.start(6969);
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
        mockAccountManagementGetEmailsEndpoint.shutdown();
    }

    @Test
    void testException() throws IOException {

        List<String> emailList = new ArrayList<>();

        mockAccountManagementGetEmailsEndpoint = new MockWebServer();
        mockAccountManagementGetEmailsEndpoint.start(6969);
        mockAccountManagementGetEmailsEndpoint.enqueue(new MockResponse().setResponseCode(404));
        try (LogCaptor logCaptor = LogCaptor.forClass(AccountManagementService.class)) {
            assertNull(accountManagementService.getEmails(emailList), "should be null when an exception is thrown");
            assertTrue(logCaptor.getErrorLogs().get(0).contains("Account management request failed for this map"),
                       "Messages do not match");
        } catch (Exception ex) {
            throw new IOException(ex);
        }
        mockAccountManagementGetEmailsEndpoint.shutdown();
    }
}
