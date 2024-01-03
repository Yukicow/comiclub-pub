package com.comiclub.web.exception.advice;


import com.comiclub.web.exception.*;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

@Slf4j
@Order(2)
@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionControllerAdvice {


//    @ResponseBody
//    @ExceptionHandler
//    public ResponseEntity<ErrorResult> RuntimeExceptionHandler(RuntimeException e) {
//        log.info("error : {}", e.getMessage());
//                log.info("errorClass : {}", e.getClass());
//        return ResponseEntity
//                .internalServerError()
//                .body(new ErrorResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"NOT_EXPECTED", e.getMessage()));
//    }




    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler
    public String notFoundExceptionHandler(NotFoundException e) {
        log.info("error : {}", e.getMessage());
        return "error/404";
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public String validationException(ValidationException e) {
        log.info("error : {}", e.getMessage());
        return "error/400";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public String ioExceptionHandler(IOException e) {
        log.info("error : {}", e.getMessage());
        return "error/500";
    }


    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler
    public String noAuthenticationExceptionHandler(NoAuthenticationException e) {
        log.info("error : {}", e.getMessage());
        return "views/login";
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler
    public String accessDeniedExceptionHandler(AccessDeniedException e) {
        log.info("error : {}", e.getMessage());
        return "views/login";
    }


    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler
    public String noAuthorizationExceptionHandler(NoAuthorizationException e) {
        log.info("error : {}", e.getMessage());
        return "error/403";
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler
    public String adultOnlyAccessibleException(AdultOnlyAccessibleException e) {
        log.info("error : {}", e.getMessage());
        return "error/adult_only";
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public String alreadyExistExceptionHandler(AlreadyExistException e) {
        log.info("error : {}", e.getMessage());
        return "error/400";
    }


}
