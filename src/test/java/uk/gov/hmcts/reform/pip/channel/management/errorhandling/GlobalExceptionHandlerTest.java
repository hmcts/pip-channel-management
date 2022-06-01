package uk.gov.hmcts.reform.pip.channel.management.errorhandling;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.ChannelNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    @Test
    void testHandleChannelNotFoundMethod() {

        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

        ChannelNotFoundException channelNotFoundException = new ChannelNotFoundException("This is a test message");

        ResponseEntity<ExceptionResponse> responseEntity =
            globalExceptionHandler.handleChannelNotFound(channelNotFoundException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), "Status code should be not found");
        assertNotNull(responseEntity.getBody(), "Response should contain a body");
        assertEquals("This is a test message", responseEntity.getBody().getMessage(),
                     "The message should match the message passed in");
    }
}
