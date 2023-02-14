package com.its.econtract.services.communication.everify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.ITSAClient;
import com.itextpdf.text.pdf.security.TSAClient;
import com.its.econtract.controllers.request.ECVerifyRequest;
import com.its.econtract.controllers.response.ECVerifyResponse;
import com.its.econtract.entity.ECVerifyConfigurationLog;
import com.its.econtract.entity.enums.ETrucServiceType;
import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.repository.ECSystemConfigurationLogRepository;
import com.its.econtract.utils.EHashUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import retrofit2.Call;
import retrofit2.Response;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Map;

import static com.its.econtract.utils.EHashUtils.decodeUsingDataTypeConverter;

@Log4j2
@Setter
@Getter
public class ETrucTSAClient implements ITSAClient, TSAClient {

    private EVerifyCommunicate eVerifyCommunicate;
    private String apiKey;
    private String secretKey;
    private ECVerifyRequest request;

    private final Gson gson = new Gson();

    private ECSystemConfigurationLogRepository systemConfigurationLogRepository;

    public ETrucTSAClient(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    @Override
    public int getTokenSizeEstimate() {
        return 4096;
    }

    @Override
    public MessageDigest getMessageDigest() throws GeneralSecurityException {
        return DigestAlgorithms.getMessageDigest("SHA-256", null);
    }

    @Override
    public byte[] getTimeStampToken(byte[] bytes) throws Exception {
        ECVerifyConfigurationLog trucLog = new ECVerifyConfigurationLog();
        try {
            String hex = EHashUtils.bytesToHex(bytes);
            request.getContent().getData().put("digest", hex);
            // signature
            request.setSignature(EVerifySignatureBuilder.builder().withRequest(request).withSecret(secretKey).toSignature());

            Call<ECVerifyResponse> res = eVerifyCommunicate.eVerifySystemAction(apiKey,
                    ETrucServiceType.E_VERIFY_SERVICE_TYPE_GET_TIME.getValue(),
                    request);
            Response<ECVerifyResponse> response = res.execute();
            if (response.isSuccessful()) {
                ECVerifyResponse dto = response.body();
                if (null == dto) {
                    throw new ECBusinessException("Can not get signature BCT");
                }
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> data = objectMapper.convertValue(dto.getContent().getData(), Map.class);
                String transactionId = (String) dto.getContent().getTransactionId();
                trucLog.setTransactionId(transactionId);
                trucLog.setCode(response.code());
                trucLog.setContent(gson.toJson(dto));
                return decodeUsingDataTypeConverter((String) data.get("timestampToken"));
            } else {
                throw new ECBusinessException("Can not get signature BCT");
            }
        } catch (Exception ex) {
            return new byte[0];
        } finally {
            log.info("Store log from Truc");
            systemConfigurationLogRepository.save(trucLog);
        }
    }
}
