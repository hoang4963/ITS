package com.its.econtract.aop;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;

import java.io.IOException;


public class ECHeaderInterceptor implements ClientHttpRequestInterceptor {
    private final String headerName;
    private final String headerValue;

    public ECHeaderInterceptor(String headerName, String headerValue) {
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
                                        ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
        HttpRequest wrapper = new HttpRequestWrapper(httpRequest);
        wrapper.getHeaders().set(headerName, headerValue);
        return clientHttpRequestExecution.execute(wrapper, bytes);
    }
}
