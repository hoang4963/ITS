package com.its.econtract.exceptions.advice;

import com.its.econtract.dto.EContractDto;
import com.its.econtract.exceptions.ECBusinessException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Log4j2
@RestControllerAdvice
public class ErrorHandlerController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<EContractDto> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        log.error("Upload Over max size:", exc);
        return EContractDto.build()
                .withHttpStatus(HttpStatus.EXPECTATION_FAILED)
                .withMessage("File too large").toResponseEntity();
    }

    @ExceptionHandler(ECBusinessException.class)
    public ResponseEntity<EContractDto> handleBusinessException(ECBusinessException exc) {
        log.error("handleBusinessException:", exc);
        return EContractDto.build()
                .withHttpStatus(exc.getStatus())
                .withMessage(exc.getMessage())
                .toResponseEntity();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<EContractDto> handleGlobalException(Exception exc) {
        log.error("handleGlobalException:", exc);
        return EContractDto.build()
                .withHttpStatus(HttpStatus.BAD_REQUEST)
                .withMessage(exc.getMessage())
                .toResponseEntity();
    }
}
