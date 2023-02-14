package com.its.econtract.signature.cloudca.communicate;

import com.its.econtract.services.communication.vvn.RetrofitAbstractCommunication;
import org.springframework.beans.factory.annotation.Value;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.annotation.PostConstruct;

public abstract class CloudCAClient extends RetrofitAbstractCommunication {

    protected CloudCACommunicate cloudCACommunicate;

    @Value(value = "${cloudCA.base_url}")
    String baseUrl;

    @PostConstruct
    public void init() {
        cloudCACommunicate = buildSetting(baseUrl);
    }

    protected CloudCACommunicate buildSetting(String baseUrl) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(this.buildCommunication())
                .build();

        return retrofit.create(CloudCACommunicate.class);
    }
}
