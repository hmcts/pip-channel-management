package uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnauthorisedExceptionTest {

    @Test
    void testCreationOfUnauthorisedException() {
        UnauthorisedException unauthorisedException = new UnauthorisedException("This is a test message");

        assertEquals("This is a test message", unauthorisedException.getMessage(),
                     "The message should match the passed in value");
    }
}
