package uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions;

/**
 * Exception that captures when an item fails to process.
 */
public class ProcessingException extends RuntimeException {

    private static final long serialVersionUID = 4330033210493138402L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public ProcessingException(String message) {
        super(message);
    }
}
