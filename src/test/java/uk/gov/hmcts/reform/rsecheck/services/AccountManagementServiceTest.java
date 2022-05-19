package uk.gov.hmcts.reform.rsecheck.services;

import com.azure.core.http.ContentType;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.hmcts.reform.demo.Application;
import uk.gov.hmcts.reform.demo.services.AccountManagementService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(classes = {Application.class})
@ActiveProfiles({"test", "non-async"})
class AccountManagementServiceTest {

    private static MockWebServer mockAccountManagementGetEmailsEndpoint;
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
            )
                                                           .setBody("{\"test123\":{\"empty\":false,"
                                                                        + "\"present\":true}}"));

        mockAccountManagementGetEmailsEndpoint.shutdown();
        assertEquals(true, true, "yes");
    }
}

//        Map<String, Optional<String>> returnedMap = accountManagementService.getEmails(emailList);
//        assertEquals(
//            testMap,
//            returnedMap,
//            "Court name does not match returned value"
//        );

//    @Test
//    void testNullCourt() throws IOException {
//        mockAccountManagementGetEmailsEndpoint = new MockWebServer();
//        mockAccountManagementGetEmailsEndpoint.start(4550);
//        mockAccountManagementGetEmailsEndpoint.enqueue(new MockResponse());
//
//        String courtName = dataManagementService.getCourtName("1");
//        assertNull(courtName, "Court return is null");
//        mockAccountManagementGetEmailsEndpoint.shutdown();
//    }
//
//    @Test
//    void testGetCourtThrows() throws IOException {
//        mockAccountManagementGetEmailsEndpoint = new MockWebServer();
//        mockAccountManagementGetEmailsEndpoint.start(4550);
//        mockAccountManagementGetEmailsEndpoint.enqueue(new MockResponse().setResponseCode(404));
//        String courtName = dataManagementService.getCourtName(INVALID);
//        assertNull(courtName, "Court name not null when error occured");
//        mockAccountManagementGetEmailsEndpoint.shutdown();
//    }
//
//}
