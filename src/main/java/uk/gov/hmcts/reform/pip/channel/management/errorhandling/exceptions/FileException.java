package uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions;

/**
 * Exception for handling files that could not retrieve input streams.
 */
public class FileException extends RuntimeException {

    private static final long serialVersionUID = 8574604777519490260L;

    /**
     * Constructor for the exception.
     */
    public FileException(String message) {
        super(message);
    }
}
