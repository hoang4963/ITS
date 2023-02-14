package com.its.econtract.vtp.service;

import com.its.econtract.services.communication.vvn.RetrofitAbstractCommunication;
import com.its.econtract.vtp.vtpekyc.VTPCommunicateV2;
import org.springframework.beans.factory.annotation.Value;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;

public abstract class VTPClientV2 extends RetrofitAbstractCommunication {

    protected VTPCommunicateV2 vtpCommunicate;
    SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmss");
    @Value(value = "${ocr.base_url}")
    String baseUrl;

    @PostConstruct
    public void init() {
        vtpCommunicate = buildSetting(baseUrl);
    }


    protected VTPCommunicateV2 buildSetting(String baseUrl) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(this.buildCommunication())
                .build();

        return retrofit.create(VTPCommunicateV2.class);
    }
}
