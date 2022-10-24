package uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions;

/**
 * Exception that captures when an item is not found.
 */
public class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 4330033210493138404L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public NotFoundException(String message) {
        super(message);
    }
}
