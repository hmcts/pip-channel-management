package uk.gov.hmcts.reform.pip.channel.management.errorhandling;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.ChannelNotFoundException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.FileSizeLimitException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.ProcessingException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.ServiceToServiceException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.UnauthorisedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private static final String MESSAGES_MATCH = "The message should match the message passed in";
    private static final String RESPONSE_BODY_MESSAGE = "Response should contain a body";
    private static final String STATUS_CODE_MATCH = "Status code should match";

    static final String TEST_MESSAGE = "This is a test message";
    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void testHandleChannelNotFoundMethod() {
        ChannelNotFoundException channelNotFoundException = new ChannelNotFoundException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handleChannelNotFound(channelNotFoundException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), STATUS_CODE_MATCH);
        assertNotNull(responseEntity.getBody(), RESPONSE_BODY_MESSAGE);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     MESSAGES_MATCH);
    }

    @Test
    void testHandleServiceToServiceException() {
        ServiceToServiceException exception = new ServiceToServiceException("Test service", TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity = globalExceptionHandler.handleServiceToService(exception);

        assertEquals(HttpStatus.BAD_GATEWAY, responseEntity.getStatusCode(), STATUS_CODE_MATCH);
        assertNotNull(responseEntity.getBody(), RESPONSE_BODY_MESSAGE);
        assertEquals("Request to Test service failed due to: " +  TEST_MESSAGE,
                     responseEntity.getBody().getMessage(), MESSAGES_MATCH);
    }

    @Test
    void testHandleNotFoundException() {
        NotFoundException exception = new NotFoundException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity = globalExceptionHandler.handleNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), STATUS_CODE_MATCH);
        assertNotNull(responseEntity.getBody(), RESPONSE_BODY_MESSAGE);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     MESSAGES_MATCH);
    }

    @Test
    void testHandleFileSizeLimitException() {
        FileSizeLimitException exception = new FileSizeLimitException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity = globalExceptionHandler
            .handleFileSizeLimitException(exception);

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, responseEntity.getStatusCode(), STATUS_CODE_MATCH);
        assertNotNull(responseEntity.getBody(), RESPONSE_BODY_MESSAGE);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(), MESSAGES_MATCH);
    }

    @Test
    void testHandleProcessingException() {
        ProcessingException exception = new ProcessingException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity = globalExceptionHandler.handleProcessingException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode(),
                     STATUS_CODE_MATCH);
        assertNotNull(responseEntity.getBody(), RESPONSE_BODY_MESSAGE);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     MESSAGES_MATCH);
    }

    @Test
    void testHandleUnauthorisedException() {
        UnauthorisedException exception = new UnauthorisedException(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> responseEntity = globalExceptionHandler
            .handleUnauthorisedException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode(),
                     STATUS_CODE_MATCH);
        assertNotNull(responseEntity.getBody(), RESPONSE_BODY_MESSAGE);
        assertEquals(TEST_MESSAGE, responseEntity.getBody().getMessage(),
                     MESSAGES_MATCH);
    }
}
