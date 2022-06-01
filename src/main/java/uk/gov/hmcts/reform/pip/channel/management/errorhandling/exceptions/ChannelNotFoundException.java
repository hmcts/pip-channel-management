package uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions;

/**
 * Exception that captures the message when a channel is not found.
 */
public class ChannelNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 4004263646614654433L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public ChannelNotFoundException(String message) {
        super(message);
    }

}
