package com.its.econtract.vtp.facade;

import com.google.common.base.Strings;
import com.its.econtract.controllers.request.EKycRequest;
import com.its.econtract.entity.ECDocumentSignatureKyc;
import com.its.econtract.entity.VtpCustomerInfo;
import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.repository.ECDocumentSignatureKycRepository;
import com.its.econtract.repository.VtpCustomerInfoRepository;
import com.its.econtract.vtp.request.*;
import com.its.econtract.vtp.response.*;
import com.its.econtract.vtp.service.VTPOCRService;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

@Log4j2
@Component
public class VTPEkycFacade {
    @Autowired
    private VTPOCRService vtpocrService;

    @Value(value = "${vtp.username}")
    String username;

    @Value(value = "${vtp.password}")
    String password;

    @Value(value = "${vtp.client_code}")
    String clientCode;

    @Autowired
    private VtpCustomerInfoRepository vtpCustomerInfoRepository;

    @Autowired
    private ECDocumentSignatureKycRepository ecDocumentSignatureKycRepository;

    @Value("${file.upload-dir}")
    private String upload;

    @SneakyThrows
    public AuthenticationResponse authen() {
        VTPAuthenticationRequest request = new VTPAuthenticationRequest();
        request.setUsername(username);
        request.setPassword(password);
        AuthenticationResponse result = vtpocrService.auth(request);
        return result;
    }

    @SneakyThrows
    public VTPOCRResponse ocr(ECOCRRequest request) {
        VtpCustomerInfo vtpCustomerInfo = vtpCustomerInfoRepository.findCustomerImage(request.getCustomerId());
        if (vtpCustomerInfo == null) throw new ECBusinessException("Not found customer info");
        AuthenticationResponse auth = authen();
        BaseResponse spoofCheckTwoImage = vtpocrService.spoofCheckTwoImage(auth.getToken(),
                new VTPSpoofCheckTwoImageRequest(clientCode, convertFileToBase64(vtpCustomerInfo.getFrontImage()),
                convertFileToBase64(vtpCustomerInfo.getBackImage())));
        if (spoofCheckTwoImage.getCode() != 1) {
            return new VTPOCRResponse(spoofCheckTwoImage,null);
        }
        VTPOCRResponse ocr = vtpocrService.ocr(auth.getToken(),
                new VTPOCRRequest(clientCode, convertFileToBase64(vtpCustomerInfo.getFrontImage()),
                        convertFileToBase64(vtpCustomerInfo.getBackImage())));

        return ocr;
    }

    @SneakyThrows
    public BaseResponse checkImage(CheckImageRequest checkImageRequest) {
        // kiểm tra chất lượng ảnh
        AuthenticationResponse auth = authen();
        BaseResponse qualityCheck = vtpocrService.qualityCheck(auth.getToken(),
                new VTPIdentificationCheckRequest(clientCode, checkImageRequest.getIdImage()));
        if (qualityCheck.getCode() != 1) {
            return qualityCheck;
        }

        // kiểm tra giả mạo ảnh
        AuthenticationResponse auth1 = authen();
        BaseResponse spoofCheck = vtpocrService.spoofCheck(auth1.getToken(),
                new VTPIdentificationCheckRequest(clientCode, checkImageRequest.getIdImage()));

        return spoofCheck;
    }


    @SneakyThrows
    public VTPKycResponse verify(EKycRequest kycRequest) {
        ECDocumentSignatureKyc ecDocumentSignatureKyc = ecDocumentSignatureKycRepository.findDocumentSignImage(kycRequest.getAssigneeId());
        if (ecDocumentSignatureKyc == null) throw new ECBusinessException("Not found assignee kyc");

        // convert File image face to base64
        String faceImageBase = convertFileToBase64(ecDocumentSignatureKyc.getFaceImage());

        // kiểm tra chất lượng ảnh live
        AuthenticationResponse auth2 = authen();
        BaseResponse faceQualityCheck = vtpocrService.faceQualityCheck(auth2.getToken(),
                new VTPIdentificationCheckRequest(clientCode, faceImageBase));
        if (faceQualityCheck.getCode() != 1) {
            return new VTPKycResponse(faceQualityCheck, false, 0.0);
        }

        // kiểm tra giả mạo ảnh live
        AuthenticationResponse auth3 = authen();
        VTPKycResponse faceLiveness = vtpocrService.faceLiveness(auth3.getToken(),
                new VTPIdentificationCheckRequest(clientCode, faceImageBase));
        if (faceLiveness.getCode() != 1) {
            return faceLiveness;
        }

        // convert File image front to base64
        String frontImageBase = convertFileToBase64(ecDocumentSignatureKyc.getFrontImage());

        // so khớp khuôn mặt
        AuthenticationResponse auth4 = authen();
        VTPKycResponse verify = vtpocrService.verify(auth4.getToken(),
                new VTPKycRequest(clientCode, frontImageBase, faceImageBase));
        validateCheck(verify);

        return verify;
    }

    private String convertFileToBase64(String f1) throws IOException {
        String file;
        if (!Strings.isNullOrEmpty(f1)) {
            File f = new File(String.format("%s%s", upload, f1));
            byte[] fileContent = FileUtils.readFileToByteArray(f);
            file = Base64.getEncoder().encodeToString(fileContent);
            return file;
        }
        throw new ECBusinessException("Not found image resource");
    }


    private void validateCheck(BaseResponse result) {
        if (result.getCode() != 1) {
            throw new ECBusinessException(result.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
