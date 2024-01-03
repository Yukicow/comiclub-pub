package com.comiclub.web.exception.advice;


import com.comiclub.web.result.ErrorResult;
import com.comiclub.web.exception.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@Order(1)
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionRestControllerAdvice {


//    
//    @ExceptionHandler
//    public String RuntimeExceptionHandler(RuntimeException e) {
//        log.info("error : {}", e.getMessage());
//                log.info("errorClass : {}", e.getClass());
//        return ResponseEntity
//                .internalServerError()
//                .body(new ErrorResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"NOT_EXPECTED", e.getMessage()));
//    }


    @ExceptionHandler
    public ResponseEntity<ErrorResult> notFoundExceptionHandler(NotFoundException e) {
        log.info("error : {}", e.getMessage());
        return ResponseEntity
                .status(HttpServletResponse.SC_NOT_FOUND)
                .body(new ErrorResult(HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResult> validationException(ValidationException e) {
        log.info("error : {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new ErrorResult(HttpServletResponse.SC_BAD_REQUEST,"NOT_VALID", e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResult> ioExceptionHandler(IOException e) {
        log.info("error : {}", e.getMessage());
        return ResponseEntity
                .status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                .body(new ErrorResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"IO_EXCEPTION", e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResult> NoAuthenticationExceptionHandler(NoAuthenticationException e) {
        log.info("error : {}", e.getMessage());
        return ResponseEntity
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .body(new ErrorResult(HttpServletResponse.SC_UNAUTHORIZED,"NO_AUTH", e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResult> accessDeniedExceptionHandler(AccessDeniedException e) {
        log.info("error : {}", e.getMessage());
        return ResponseEntity
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .body(new ErrorResult(HttpServletResponse.SC_UNAUTHORIZED,"NO_AUTH", e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResult> NoAuthorizationExceptionHandler(NoAuthorizationException e) {
        log.info("error : {}", e.getMessage());
        return ResponseEntity
                .status(HttpServletResponse.SC_FORBIDDEN)
                .body(new ErrorResult(HttpServletResponse.SC_FORBIDDEN,"NO_AUTH", e.getMessage()));
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler
    public ResponseEntity<ErrorResult> adultOnlyAccessibleException(AdultOnlyAccessibleException e) {
        log.info("error : {}", e.getMessage());
        return ResponseEntity
                .status(HttpServletResponse.SC_FORBIDDEN)
                .body(new ErrorResult(HttpServletResponse.SC_FORBIDDEN,"NO_AUTH", e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResult> alreadyExistExceptionHandler(AlreadyExistException e) {
        log.info("error : {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new ErrorResult(HttpServletResponse.SC_BAD_REQUEST,"ALREADY_EXIST", e.getMessage()));
    }


    @ExceptionHandler
    public ResponseEntity<ErrorResult> autoModeFalseExceptionHandler(AutoModeFalseException e) {
        log.info("error : {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new ErrorResult(HttpServletResponse.SC_BAD_REQUEST,"AUTO_MODE_FALSE", e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResult> SQLIntegrityConstraintViolationExceptionHandler(SQLIntegrityConstraintViolationException e) {
        log.info("error : {}", e.getMessage());
        return ResponseEntity
                .status(HttpServletResponse.SC_NOT_FOUND)
                .body(new ErrorResult(HttpServletResponse.SC_NOT_FOUND,"NOT_EXIST", e.getMessage()));
    }


}
