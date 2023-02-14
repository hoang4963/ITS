package com.its.econtract.services.communication.everify;

import com.google.gson.Gson;
import com.its.econtract.controllers.request.ECVerifyRequest;
import com.its.econtract.utils.EHashUtils;

public class EVerifySignatureBuilder {

    private Gson gson;

    private ECVerifyRequest ecVerifyRequest;
    private String secretKey;
    private EVerifySignatureBuilder() {
        this.gson = new Gson();
    }

    public static EVerifySignatureBuilder builder() {
        return new EVerifySignatureBuilder();
    }

    public EVerifySignatureBuilder withRequest(ECVerifyRequest request) {
        this.ecVerifyRequest = request;
        return this;
    }

    public EVerifySignatureBuilder withSecret(String secret) {
        this.secretKey = secret;
        return this;
    }

    public String toSignature() {
        try {
            String info = gson.toJson(ecVerifyRequest.getInfo());
            String content = gson.toJson(ecVerifyRequest.getContent());
            String preHash = EHashUtils.hashSHA256(info) + "." + EHashUtils.hashSHA256(content);
            return EHashUtils.hmacWithJava(preHash.toUpperCase(), secretKey).toUpperCase();
        } catch (Exception e) {
            return "";
        }
    }
}
