package com.its.econtract.vtp.service;

import com.its.econtract.services.communication.vvn.RetrofitAbstractCommunication;
import com.its.econtract.vtp.vtpekyc.VTPCommunicate;
import org.springframework.beans.factory.annotation.Value;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.annotation.PostConstruct;

public abstract class VTPClient extends RetrofitAbstractCommunication {

    protected VTPCommunicate vtpCommunicate;

    @Value(value = "${vtp.base_url}")
    String baseUrl;

    @PostConstruct
    public void init() {
        vtpCommunicate = buildSetting(baseUrl);
    }


    protected VTPCommunicate buildSetting(String baseUrl) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(this.buildCommunication())
                .build();

        return retrofit.create(VTPCommunicate.class);
    }
}
