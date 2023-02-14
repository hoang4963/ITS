package com.its.econtract.signature.cloudca.service;

import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.signature.cloudca.ca.CertBO;
import com.its.econtract.signature.cloudca.ca.CredentialsAuthorizeRequestBO;
import com.its.econtract.signature.cloudca.ca.CredentialsAuthorizeResponceBO;
import com.its.econtract.signature.cloudca.ca.CredentialsInfoRequestBO;
import com.its.econtract.signature.cloudca.ca.CredentialsInfoResponceBO;
import com.its.econtract.signature.cloudca.ca.CredentialsListRequestBO;
import com.its.econtract.signature.cloudca.ca.CredentialsListResponceBO;
import com.its.econtract.signature.cloudca.ca.LoginRequestBO;
import com.its.econtract.signature.cloudca.ca.LoginResponceBO;
import com.its.econtract.signature.cloudca.ca.SignHashRequestBO;
import com.its.econtract.signature.cloudca.ca.SignHashResponceBO;
import com.its.econtract.signature.cloudca.communicate.CloudCAService;
import com.its.econtract.signature.cloudca.response.CredentialsListResponse;
import com.viettel.signature.utils.CertUtils;
import io.reactivex.rxjava3.core.Observable;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;
import java.util.*;

@Log4j2
@Component
public class ECMySiteService {

    @Autowired
    private CloudCAService cloudCAService;

    @Value("${client_id}")
    private String clientId;

    @Value("${client_secret}")
    private String clientSecret;

    @Value("${profile_id}")
    private String profileId;


    @SneakyThrows
    public LoginResponceBO authen(String userId) {
        LoginResponceBO result = cloudCAService.auth(new LoginRequestBO(clientId, userId, clientSecret, profileId));
        return result;
    }

    public CredentialsInfoResponceBO getCredentialsInfo(String accessToken, CredentialsInfoRequestBO credentialsInfoRequestBO) {
        return cloudCAService.getCredentialsInfo(accessToken, credentialsInfoRequestBO);
    }

    public CredentialsAuthorizeResponceBO credentialsAuthorize(String accessToken, CredentialsAuthorizeRequestBO requestBO) {
        return cloudCAService.credentialsAuthorize(accessToken, requestBO);
    }

    public SignHashResponceBO signHash(String accessToken, SignHashRequestBO signHashRequestBO) {
        return cloudCAService.signHash(accessToken, signHashRequestBO);
    }

    @SneakyThrows
    public List<CredentialsListResponse> getListCredentials(CredentialsListRequestBO requestBO) {
        List<CredentialsListResponse> listResponses = new ArrayList<>();
        // step 1 : login
        LoginResponceBO auth = authen(requestBO.getUserID());

        // step 2 : get list credentials
        CredentialsListResponceBO credentialsListResponceBO = cloudCAService.getListCredentials(auth.getAccess_token(), requestBO);

        // step 3 : get info credentials
        //TODO: please using rxJava
        HashMap<String, CertBO> certList = new HashMap<>();
//        for (String credentialID : credentialsListResponceBO.getCredentialIDs()) {
//            CredentialsInfoResponceBO credentialsInfoResponceBO = cloudCAService.getCredentialsInfo(auth.getAccess_token(),
//                    new CredentialsInfoRequestBO(credentialID));
//            certList.put(credentialID, credentialsInfoResponceBO.getCert());
//        }
        Observable.fromIterable(credentialsListResponceBO.getCredentialIDs())
                .map(credentialID -> new CredentialsInfoRequestBO(credentialID))
                .toMap(credentialsInfoRequestBO -> credentialsInfoRequestBO.getCredentialID(), credentialsInfoRequestBO -> cloudCAService.getCredentialsInfo(auth.getAccess_token(),credentialsInfoRequestBO).getCert())
                .doOnSuccess(certList::putAll)
                .subscribe();
            if (certList.size() == 0) {
                throw new ECBusinessException("Không tìm thấy CTS");
            }

//        for (Map.Entry<String, CertBO> entry : certList.entrySet()) {
//            String key = entry.getKey();
//            CertBO certBO = entry.getValue();
//            if (certBO != null && certBO.getCertificates() != null && certBO.getCertificates().size() != 0) {
//                X509Certificate x509Cert = CertUtils.getX509Cert(certBO.getCertificates().get(0));
//                CredentialsListResponse response = new CredentialsListResponse();
//                response.setKey(key);
//                response.setName(x509Cert.getSerialNumber().toString(16) + " - " + x509Cert.getNotAfter().toGMTString() + " - " + CertUtils.getCN(x509Cert));
//                listResponses.add(response);
//            }
//        }
        Observable.fromIterable(certList.entrySet())
                .filter(entry -> entry.getValue() != null && entry.getValue().getCertificates() != null && entry.getValue().getCertificates().size() != 0)
                .map(entry -> {
                    String key = entry.getKey();
                    CertBO certBO = entry.getValue();
                    X509Certificate x509Cert = CertUtils.getX509Cert(certBO.getCertificates().get(0));
                    CredentialsListResponse response = new CredentialsListResponse();
                    response.setKey(key);
                    response.setName(x509Cert.getSerialNumber().toString(16) + " - " + x509Cert.getNotAfter().toString() + " - " + CertUtils.getCN(x509Cert));
                    return response;
                })
                .toList()
                .doOnSuccess(listResponses::addAll)
                .subscribe();

        // return list
        return listResponses;
    }
}
