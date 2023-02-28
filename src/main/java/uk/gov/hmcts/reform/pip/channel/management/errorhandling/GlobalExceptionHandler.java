package uk.gov.hmcts.reform.pip.channel.management.errorhandling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.ChannelNotFoundException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.ProcessingException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.ServiceToServiceException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.UnauthorisedException;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

/**
 * Global exception handler, that captures exceptions thrown by the controllers, and encapsulates
 * the logic to handle them and return a standardised response to the user.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Template exception handler, that handles a custom ChannelNotFoundException,
     * and returns a 404 in the standard format.
     * @param ex The exception that has been thrown.
     * @return The error response, modelled using the ExceptionResponse object.
     */
    @ExceptionHandler(ChannelNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleChannelNotFound(ChannelNotFoundException ex) {
        log.error("Channel not found exception thrown: {}", ex.getMessage());

        log.error(writeLog("404, no channels found for any subscribers"));

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    @ExceptionHandler(ServiceToServiceException.class)
    public ResponseEntity<ExceptionResponse> handleServiceToService(ServiceToServiceException ex) {
        log.error(String.format("ServiceToServiceException was thrown with the init cause: %s", ex.getCause()));

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(exceptionResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFound(NotFoundException ex) {
        log.error(String.format("NotFoundException was thrown with the init cause: %s", ex.getCause()));

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    @ExceptionHandler(ProcessingException.class)
    public ResponseEntity<ExceptionResponse> handleProcessingException(ProcessingException ex) {
        log.error(String.format("ProcessingException was thrown with the init cause: %s", ex.getCause()));

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionResponse);
    }

    @ExceptionHandler(UnauthorisedException.class)
    public ResponseEntity<ExceptionResponse> handleUnauthorisedException(UnauthorisedException ex) {
        log.error(String.format("UnauthorisedException was thrown with the init cause: %s", ex.getCause()));

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(ex.getMessage());
        exceptionResponse.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionResponse);
    }
}
