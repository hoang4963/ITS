package com.its.econtract.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.annotation.PostConstruct;
import java.util.Map;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EContractDto<T> {
    @JsonIgnore
    private HttpStatus httpStatus = HttpStatus.OK;
    @JsonIgnore
    private HttpHeaders headers;

    private int code;

    private String message;

    private T data;

    private Map<String, Object> errors;

    public static <T> EContractDto<T> build() {
        return new EContractDto<>();
    }

    @PostConstruct
    private void init() {
        httpStatus = HttpStatus.OK;
        code = httpStatus.value();
        errors = Maps.newConcurrentMap();
    }

    public EContractDto<T> withHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        this.code = httpStatus.value();
        return this;
    }

    public EContractDto<T> withCode(int code){
        this.code = code;
        return this;
    }

    public EContractDto<T> withData(T data) {
        this.data = data;
        return this;
    }

    public EContractDto<T> withHttpHeaders(HttpHeaders httpHeaders) {
        this.headers = httpHeaders;
        return this;
    }

    public EContractDto<T> withMessage(String message) {
        this.message = message;
        return this;
    }

    public EContractDto<T> withErrors(Map<String, Object> errors) {
        this.errors = errors;
        return this;
    }

    public ResponseEntity<EContractDto> toResponseEntity() {
        return new ResponseEntity<>(this, headers, httpStatus);
    }
}
