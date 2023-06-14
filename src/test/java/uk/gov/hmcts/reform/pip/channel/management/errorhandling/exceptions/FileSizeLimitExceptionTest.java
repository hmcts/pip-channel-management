package uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileSizeLimitExceptionTest {
    @Test
    void testFileSizeLimitException() {
        FileSizeLimitException exception = new FileSizeLimitException("This is a test message");
        assertEquals("This is a test message", exception.getMessage(),
                     "The message should match the message passed in");
    }
}
