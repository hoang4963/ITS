/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.its.econtract.services.communication.vvn;

import com.its.econtract.utils.MessageUtils;
import com.its.econtract.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author quangdt
 */
public abstract class VVNClient extends RetrofitAbstractCommunication {

    @Autowired
    MessageUtils messageUtil;

    VVNCommunicate vvnCommunicate;
    SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmss");

    @Value(value = "${ocr.base_url}")
    String baseUrl;

    @Value(value = "${ai.key}")
    protected String token;

    @PostConstruct
    public void init() {
        vvnCommunicate = buildSetting(baseUrl);
    }

    protected synchronized String getRequestId(String sessionId){
        return sdf.format(new Date())+"-"+sessionId+"-"+ StringUtil.randomString(7);
    }

    protected VVNCommunicate buildSetting(String baseUrl) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(this.buildCommunication())
                .build();

        return retrofit.create(VVNCommunicate.class);
    }
}
