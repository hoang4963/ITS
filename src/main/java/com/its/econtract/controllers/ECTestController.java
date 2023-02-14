package com.its.econtract.controllers;

import com.google.gson.Gson;
import com.itextpdf.signatures.PrivateKeySignature;
import com.its.econtract.dto.EContractDto;
import com.its.econtract.entity.ECVerifyConfiguration;
import com.its.econtract.services.communication.everify.EVerifyCommunicateService;
import com.its.econtract.services.communication.everify.EVerifyCommunicateService2;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.its.econtract.utils.EHashUtils.bytesToHex;
import static com.its.econtract.utils.EHashUtils.decodeUsingGuava;

@Log4j2
@RestController
@RequestMapping(value = "/api/v1")
public class ECTestController {

    @Autowired
    private EVerifyCommunicateService eVerifyCommunicateService;
    @Autowired
    private EVerifyCommunicateService2 eVerifyCommunicateService2;

    @GetMapping("/e-verify/certificate")
    public ResponseEntity getSignature() {
        ECVerifyConfiguration configuration = eVerifyCommunicateService.getEVerifySignature();
        return EContractDto.build().withData(configuration).withHttpStatus(HttpStatus.OK).withMessage("Successfully").toResponseEntity();
    }

    @GetMapping("/e-verify/test")
    public ResponseEntity sendVerifyDocument2() throws Exception {
        eVerifyCommunicateService2.uploadDummyData("/Users/namnv/Downloads/HDLD_DDD.pdf", 163);
        return EContractDto.build().withData(1).withHttpStatus(HttpStatus.OK).withMessage("Successfully").toResponseEntity();
    }

    @GetMapping("/e-verify/test2")
    public ResponseEntity sendVerifyDocument3() throws Exception {
        eVerifyCommunicateService2.getHistory("06EBA5ADD7DF4FBE98DBBF1ED036CCE9", 12);
        return EContractDto.build().withData(1).withHttpStatus(HttpStatus.OK).withMessage("Successfully").toResponseEntity();
    }

    @GetMapping("/e-verify/test3")
    public ResponseEntity getTestResponse() {
        return EContractDto.build().withData(1).withHttpStatus(HttpStatus.OK).withMessage("Successfully").toResponseEntity();
    }

    char[] PASSWORD = "ItsCA2022".toCharArray();
    @PostMapping("sign")
    public ResponseEntity signTest(@RequestBody List<String> messageHash) throws GeneralSecurityException, IOException {
        log.info("begin sign data {}", messageHash);
        BouncyCastleProvider providerBC = new BouncyCastleProvider();
        Security.addProvider(providerBC);
        KeyStore ks = KeyStore.getInstance("jks");
        ks.load(new FileInputStream("/Users/namnv/Documents/Projects/ILTs/eContract-backgroud/src/main/resources/ceca-keystore.jks"), PASSWORD);
        String alias = ks.aliases().nextElement();
        System.out.println(alias);
        byte[] inputs = decodeUsingGuava(messageHash.get(0));
        Signature sig = Signature.getInstance("SHA256withRSA");
        PrivateKey pk = (PrivateKey) ks.getKey("ItsCA", PASSWORD);
        sig.initSign(pk);
        sig.update(inputs);
        byte[] data = sig.sign();
        log.info("end sign data = {}", bytesToHex(data));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream")).body(data);
    }

    @PostMapping("pre-sign")
    public ResponseEntity preSign(@RequestBody byte[] messageHash) throws GeneralSecurityException, IOException {
        log.info("begin sign data");
        BouncyCastleProvider providerBC = new BouncyCastleProvider();
        Security.addProvider(providerBC);
        KeyStore ks = KeyStore.getInstance("jks");
        ks.load(new FileInputStream("/Users/namnv/Documents/Projects/ILTs/eContract-backgroud/src/main/resources/ceca-keystore.jks"), PASSWORD);
        String alias = ks.aliases().nextElement();
        System.out.println(alias);
        PrivateKey pk = (PrivateKey) ks.getKey("ItsCA", PASSWORD);
        PrivateKeySignature signature = new PrivateKeySignature(pk, "SHA256", "BC");
        byte[] extSignature = signature.sign(messageHash);
        log.info("end sign data = {}", bytesToHex(extSignature));
        return ResponseEntity.ok().body(extSignature);
    }

    @PostMapping("forward")
    public ResponseEntity forwarder(@RequestBody List<String> hashes) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(150, TimeUnit.SECONDS)
                .writeTimeout(150, TimeUnit.SECONDS)
                .readTimeout(150, TimeUnit.SECONDS)
                .build();    // socket timeout
        try {
            Gson gson = new Gson();
            okhttp3.RequestBody body = okhttp3.RequestBody.create(gson.toJson(hashes), okhttp3.MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("http://192.168.31.190:6789/api/sign/hash?serialNumber=5404FFFEB7033FB316D672201B8E966C")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            String data = response.body().string();
            log.info("data: {}", data);
            return ResponseEntity.ok().body(data);
        } catch (Exception e) {
            throw e;
        }
    }
}
