package uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthorisedExceptionTest {

    @Test
    void testCreationOfAuthorisedException() {
        AuthorisedException authorisedException = new AuthorisedException("This is a test message");

        assertEquals("This is a test message", authorisedException.getMessage(),
                     "The message should match the passed in value");
    }
}
