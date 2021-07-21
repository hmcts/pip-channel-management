package uk.gov.hmcts.reform.rsecheck.errorhandling.exceptions;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.demo.errorhandling.exceptions.ChannelNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChannelNotFoundExceptionTest {

    @Test
    public void testCreationOfSubscriptionNotFoundException() {

        ChannelNotFoundException subscriptionNotFoundException
            = new ChannelNotFoundException("This is a test message");
        assertEquals("This is a test message", subscriptionNotFoundException.getMessage(),
                     "The message should match the message passed in");

    }

}
