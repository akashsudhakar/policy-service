package com.embea.policy.exception.handlers;

import com.embea.policy.exception.PolicyNotFoundException;
import com.embea.policy.model.ErrorResponse;
import java.util.Date;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException notValidException,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    ErrorResponse errorResponse =
        new ErrorResponse(
            new Date(),
            notValidException.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("Unknown error"),
            "Invalid input provided. Look at message for specific fields which are invalid.");
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  /**
   * Exception handler for PolicyNotFoundException
   *
   * @param ex Exception thrown
   * @param request WebRequest
   * @return Error response in the required format
   */
  @ResponseBody
  @ExceptionHandler(PolicyNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleMessagePublishFailedException(
      PolicyNotFoundException ex, WebRequest request) {
    ErrorResponse errorResponse =
        new ErrorResponse(new Date(), "Policy not found.", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ResponseBody
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
    ErrorResponse errorResponse =
        new ErrorResponse(new Date(), ex.getMessage(), request.getDescription(false));
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
