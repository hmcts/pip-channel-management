package uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessingExceptionTest {

    @Test
    void testCreationOfProcessingException() {
        ProcessingException processingException = new ProcessingException("This is a test message");

        assertEquals("This is a test message", processingException.getMessage(),
                     "The message should match the passed in value");
    }
}
