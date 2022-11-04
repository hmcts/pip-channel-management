package uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotFoundExceptionTest {

    @Test
    void testCreationOfNotFoundException() {
        NotFoundException notFoundException = new NotFoundException("This is a test message");

        assertEquals("This is a test message", notFoundException.getMessage(),
                     "The message should match the passed in value");
    }
}
