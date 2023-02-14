package com.its.econtract.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
public class ECBusinessException extends RuntimeException {

    private HttpStatus status = HttpStatus.BAD_REQUEST;

    private String message;

    private String[] errors = new String[]{};

    public ECBusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.message = message;
    }

    public ECBusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.message = message;
    }

    public ECBusinessException(String message, HttpStatus status, String[] errors) {
        super(message);
        this.status = status;
        this.message = message;
        this.errors = errors;
    }


    public ECBusinessException(String message, int code) {
        super(message);
        this.status = HttpStatus.valueOf(code);
        this.message = message;
    }
}
