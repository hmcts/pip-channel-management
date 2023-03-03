package uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions;

/**
 * Exception that captures when a user is not authorised to access what has been requested.
 */
public class UnauthorisedException extends RuntimeException {

    private static final long serialVersionUID = 4330033210493138404L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public UnauthorisedException(String message) {
        super(message);
    }
}
