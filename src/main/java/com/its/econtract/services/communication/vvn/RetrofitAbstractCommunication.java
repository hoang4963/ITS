/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.its.econtract.services.communication.vvn;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author quangdt
 */
public class RetrofitAbstractCommunication {
    @Value("${server.max_request}")
    protected int maxRequest = 100;
    @Value("${server.max_request_per_host}")
    protected int maxRequestPerHost = 0;

    @Value("${server.timeout.connect}")
    private long connectTimeout = 30;

     @Value("${server.timeout.read}")
    private long readTimeout = 60;

     @Value("${dispatcher.poolsize}")
     int dispatcherThreadPoolSize;

    protected OkHttpClient buildCommunication() {
        Dispatcher dispatcher = new Dispatcher(CustomExecutors.newCachedThreadPool(dispatcherThreadPoolSize));
        dispatcher.setMaxRequests(maxRequest);
        if(maxRequestPerHost > 0){
            dispatcher.setMaxRequestsPerHost(maxRequestPerHost);
        }
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        logger.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .addInterceptor(logger)
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(8, 60, TimeUnit.SECONDS)).build();

        return okHttpClient;
    }

}
