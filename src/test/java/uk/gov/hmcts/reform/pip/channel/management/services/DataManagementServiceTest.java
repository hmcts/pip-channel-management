package uk.gov.hmcts.reform.pip.channel.management.services;

import com.azure.core.http.ContentType;
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
import uk.gov.hmcts.reform.pip.channel.management.config.WebClientTestConfiguration;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.ServiceToServiceException;
import uk.gov.hmcts.reform.pip.model.location.Location;
import uk.gov.hmcts.reform.pip.model.publication.Artefact;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("PMD.TooManyMethods")
@SpringBootTest(classes = {Application.class, WebClientTestConfiguration.class})
@ActiveProfiles("test")
class DataManagementServiceTest {

    private static final  String LOCATION_ID = "1234";

    private static final String RESPONSE_BODY = "responseBody";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String EXCEPTION_RESPONSE_MESSAGE =
        "Exception response does not contain the status code in the message";

    private static MockWebServer mockDataManagementServicesEndpoint;

    private static final String INTERNAL_ERROR = "500";
    private static final String NO_STATUS_CODE_IN_EXCEPTION = "Exception response does not contain the status code in"
        + " the message";
    private static final String NO_EXPECTED_EXCEPTION = "Expected exception has not been thrown.";
    private static final String NOT_FOUND_MESSAGE = "Not found response message does not match";

    @Autowired
    WebClient webClient;

    @Autowired
    DataManagementService dataManagementService;

    private UUID uuid;

    @BeforeEach
    void setup() throws IOException {
        mockDataManagementServicesEndpoint = new MockWebServer();
        mockDataManagementServicesEndpoint.start(8090);
        uuid = UUID.randomUUID();
    }

    @AfterEach
    void after() throws IOException {
        mockDataManagementServicesEndpoint.close();
    }

    @Test
    void testGetArtefactReturnsOk() {
        mockDataManagementServicesEndpoint.enqueue(new MockResponse()
                                                    .addHeader(CONTENT_TYPE_HEADER,
                                                               ContentType.APPLICATION_JSON)
                                                    .setBody("{\"artefactId\": \"" + uuid + "\"}")
                                                    .setResponseCode(200));

        Artefact artefact = dataManagementService.getArtefact(uuid);
        assertEquals(uuid, artefact.getArtefactId(), "Returned artefact does not match expected artefact");
    }

    @Test
    void testGetArtefactReturnsNotFound() {
        mockDataManagementServicesEndpoint.enqueue(new MockResponse().setResponseCode(404));

        NotFoundException notFoundException = assertThrows(NotFoundException.class, () ->
                                                                     dataManagementService.getArtefact(uuid),
                                                                 NO_EXPECTED_EXCEPTION);

        assertEquals(String.format("Artefact with id %s not found", uuid), notFoundException.getMessage(),
                     NOT_FOUND_MESSAGE);
    }

    @Test
    void testGetArtefactReturnsException() {
        mockDataManagementServicesEndpoint.enqueue(new MockResponse().setResponseCode(501));

        ServiceToServiceException notifyException = assertThrows(ServiceToServiceException.class, () ->
                                                                     dataManagementService.getArtefact(uuid),
                                                                 NO_EXPECTED_EXCEPTION);

        assertTrue(notifyException.getMessage().contains("501"),
                   EXCEPTION_RESPONSE_MESSAGE);
    }

    @Test
    void testGetLocationReturnsOk() {
        String locationName = "locationName";

        mockDataManagementServicesEndpoint.enqueue(new MockResponse()
                                                    .addHeader(CONTENT_TYPE_HEADER,
                                                               ContentType.APPLICATION_JSON)
                                                    .setBody("{\"name\": \"" + locationName + "\"}")
                                                    .setResponseCode(200));

        Location location = dataManagementService.getLocation(LOCATION_ID);
        assertEquals(locationName, location.getName(), "Returned location does not match expected location");
    }

    @Test
    void testGetLocationReturnsNotFound() {
        mockDataManagementServicesEndpoint.enqueue(new MockResponse().setResponseCode(404));

        NotFoundException notFoundException = assertThrows(NotFoundException.class, () ->
                                                               dataManagementService.getLocation(LOCATION_ID),
                                                           NO_EXPECTED_EXCEPTION);

        assertEquals(String.format("Location with id %s not found", LOCATION_ID), notFoundException.getMessage(),
                     NOT_FOUND_MESSAGE);
    }

    @Test
    void testGetLocationReturnsException() {
        mockDataManagementServicesEndpoint.enqueue(new MockResponse().setResponseCode(500));

        ServiceToServiceException notifyException = assertThrows(ServiceToServiceException.class, () ->
                                                                     dataManagementService.getLocation(LOCATION_ID),
                                                                 NO_EXPECTED_EXCEPTION);

        assertTrue(notifyException.getMessage().contains(INTERNAL_ERROR),
                   NO_STATUS_CODE_IN_EXCEPTION);
    }

    @Test
    void testGetArtefactJsonBlobReturnsOk() {
        mockDataManagementServicesEndpoint.enqueue(new MockResponse()
                                                    .addHeader(CONTENT_TYPE_HEADER,
                                                               ContentType.APPLICATION_JSON)
                                                    .setBody(RESPONSE_BODY)
                                                    .setResponseCode(200));

        String returnedContent = dataManagementService.getArtefactJsonBlob(uuid);
        assertEquals(RESPONSE_BODY, returnedContent, "Returned payload does not match expected data");
    }

    @Test
    void testGetArtefactJsonPayload() {
        mockDataManagementServicesEndpoint.enqueue(new MockResponse()
                                                    .setBody("testJsonString")
                                                    .setResponseCode(200));
        String jsonPayload = dataManagementService.getArtefactJsonBlob(uuid);
        assertEquals("testJsonString", jsonPayload, "Messages do not match");
    }

    @Test
    void testGetArtefactJsonPayloadReturnsNotFound() {
        mockDataManagementServicesEndpoint.enqueue(new MockResponse().setResponseCode(404));

        NotFoundException notFoundException = assertThrows(NotFoundException.class, () ->
                                                               dataManagementService.getArtefactJsonBlob(uuid),
                                                           NO_EXPECTED_EXCEPTION);

        assertEquals(String.format("Artefact with id %s not found", uuid), notFoundException.getMessage(),
                     NOT_FOUND_MESSAGE);
    }

    @Test
    void testFailedGetArtefactJsonPayload() {
        UUID uuid = UUID.randomUUID();
        mockDataManagementServicesEndpoint.enqueue(new MockResponse().setResponseCode(500));

        ServiceToServiceException notifyException = assertThrows(ServiceToServiceException.class, () ->
                                                                     dataManagementService.getArtefactJsonBlob(uuid),
                                                                 NO_EXPECTED_EXCEPTION);
        assertTrue(notifyException.getMessage().contains(INTERNAL_ERROR),
                   NO_STATUS_CODE_IN_EXCEPTION);
    }
}
