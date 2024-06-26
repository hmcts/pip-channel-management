package uk.gov.hmcts.reform.pip.channel.management.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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
import java.util.UUID;

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
