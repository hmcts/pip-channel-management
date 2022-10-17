package uk.gov.hmcts.reform.pip.channel.management.errorhandling;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.ChannelNotFoundException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.ServiceToServiceException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private static final String MESSAGES_MATCH = "The message should match the message passed in";
    private static final String RESPONSE_BODY_MESSAGE = "Response should contain a body";

    static final String TEST_MESSAGE = "This is a test message";
    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void testHandleChannelNotFoundMethod() {
        ChannelNotFoundException channelNotFoundException = new ChannelNotFoundException("This is a test message");

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handleChannelNotFound(channelNotFoundException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), "Status code should be not found");
        assertNotNull(responseEntity.getBody(), RESPONSE_BODY_MESSAGE);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     MESSAGES_MATCH);
    }

    @Test
    void testHandleServiceToServiceException() {
        ServiceToServiceException exception = new ServiceToServiceException("Test service", TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity = globalExceptionHandler.handleServiceToService(exception);

        assertEquals(HttpStatus.BAD_GATEWAY, responseEntity.getStatusCode(), "Status code should be bad gateway");
        assertNotNull(responseEntity.getBody(), RESPONSE_BODY_MESSAGE);
        assertEquals("Request to Test service failed due to: " +  TEST_MESSAGE,
                     responseEntity.getBody().getMessage(), MESSAGES_MATCH);
    }
}
