package com.its.econtract.services;

import com.google.common.base.Strings;
import com.its.econtract.dto.KYCResponseDto;
import com.its.econtract.dto.OCRBackResponseDto;
import com.its.econtract.dto.OCRFrontResponseDto;
import com.its.econtract.entity.ECDocumentAssignee;
import com.its.econtract.entity.ECDocumentSignatureKyc;
import com.its.econtract.entity.enums.ECKycLogType;
import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.entity.enums.ECKycType;
import com.its.econtract.controllers.request.EKycRequest;
import com.its.econtract.repository.ECDocumentAssigneeRepository;
import com.its.econtract.repository.ECDocumentSignatureKycRepository;
import com.its.econtract.utils.MessageUtils;
import com.its.econtract.utils.StringUtil;
import com.its.econtract.services.communication.vvn.VVNOCRService;
import com.its.econtract.controllers.response.KYCResponse;
import com.its.econtract.controllers.response.OCRResponse;
import com.its.econtract.vtp.response.BaseResponse;
import com.its.econtract.vtp.response.VTPKycResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Log4j2
@Component
public class OCRService {
    @Autowired
    private VVNOCRService vvnocrService;

    @Autowired
    MessageUtils messageUtil;

    @Autowired
    private ECDocumentAssigneeRepository assigneeRepository;

    @Autowired
    private ECDocumentSignatureKycRepository ecDocumentSignatureKycRepository;

    @Value("${file.upload-dir}")
    private String upload;

    @Value(value = "${confident.limit}")
    float confidentLimit;

//    @SneakyThrows
//    public OCRFrontResponseDto ocrFrontResponseDto(OCRRequest ocrRequest) {
//        String sessionId = StringUtil.randomString(7);
//        OCRResponse result = vvnocrService.ocr(sessionId, ocrRequest.getFile().getBytes(), ocrRequest.getFile().getName());
//        validateResult(result);
//
//        return new OCRFrontResponseDto().getOCRFrontResponseDto(result);
//    }
//
//    private void validateResult(OCRResponse result) {
//        if ("".equals(result.getIdType())) {
//            log.error("Unable to distinguish faces {}. id_type null", result.getIdType());
//            throw new ECBusinessException("Unable to distinguish faces", HttpStatus.BAD_REQUEST);
//        }
//        if (MessageUtils.NA.equals(result.getId())) {
//            log.error("ID not found {}.", result.getId());
//            throw new ECBusinessException("ID not found", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//        if (MessageUtils.NA.equals(result.getName())) {
//            log.error("Name not found {}.", result.getName());
//            throw new ECBusinessException("Name not found", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//        if (MessageUtils.NA.equalsIgnoreCase(result.getBirthday())) {
//            log.error("Birthday not found {}.", result.getBirthday());
//            throw new ECBusinessException("Birthday not found", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
////        if (MessageUtils.NA.equalsIgnoreCase(result.getSex())) {
////            log.error("Sex not found {}.", result.getSex());
////            throw new ECBusinessException("Sex not found", HttpStatus.INTERNAL_SERVER_ERROR);
////        }
//        if (MessageUtils.NA.equalsIgnoreCase(result.getAddress())) {
//            log.error("Address not found {}.", result.getAddress());
//            throw new ECBusinessException("Address not found", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//        if (MessageUtils.NA.equalsIgnoreCase(result.getHometown())) {
//            log.error("Hometown not found {}.", result.getHometown());
//            throw new ECBusinessException("Hometown not found", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    private void validateBackResult(OCRResponse result) {
//        if ("".equals(result.getIdType())) {
//            log.error("Unable to distinguish faces {}. id_type null", result.getIdType());
//            throw new ECBusinessException("Unable to distinguish faces", HttpStatus.BAD_REQUEST);
//        }
//        if (result.getIssueDate().equals(MessageUtils.NA)) {
//            log.error("issue date not found {}.", result.getIssueDate());
//            throw new ECBusinessException("issue date not found", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//        if (result.getIssueBy().equals(MessageUtils.NA)) {
//            log.error("issue by not found {}.", result.getIssueBy());
//            throw new ECBusinessException("issue by not found", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @SneakyThrows
//    public OCRBackResponseDto ocrBackResponseDto(OCRRequest ocrRequest) {
//        String sessionId = StringUtil.randomString(7);
//        OCRResponse result = vvnocrService.ocr(sessionId, ocrRequest.getFile().getBytes(), ocrRequest.getFile().getName());
//        if ("".equals(result.getIdType())) {
//            log.error("Unable to distinguish faces {}. id_type null", result.getIdType());
//            throw new ECBusinessException("Unable to distinguish faces", HttpStatus.BAD_REQUEST);
//        }
//        if (result.getIssueDate().equals(MessageUtils.NA)) {
//            log.error("issue date not found {}.", result.getIssueDate());
//            throw new ECBusinessException("issue date not found", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//        if (result.getIssueBy().equals(MessageUtils.NA)) {
//            log.error("issue by not found {}.", result.getIssueBy());
//            throw new ECBusinessException("issue by not found", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//
//        return new OCRBackResponseDto().getOCRBackResponseDto(result);
//    }
//
//    @SneakyThrows
//    public KYCResponseDto kycResponseDto(KYCRequest kycRequest) {
//        String sessionId = StringUtil.randomString(7);
//        KYCResponse result = vvnocrService.verify(sessionId, kycRequest.getIdImage().getBytes(), kycRequest.getIdImage().getName(),
//                kycRequest.getFaceImage().getBytes(), kycRequest.getFaceImage().getName());
//        validateEKyc(result);
//        return new KYCResponseDto().getKYCResponseDto(result);
//    }
//
//    private void validateEKyc(KYCResponse result) {
//        if (result.getMessage().getErrorCode().equals(MessageUtils.ERR_005)) {
//            log.error(result.getMessage().getErrorMessage());
//            throw new ECBusinessException(result.getMessage().getErrorMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//        if (result.getFaceAntiSpoofStatus().getStatus().equals(MessageUtils.FAKE)) {
//            throw new ECBusinessException(MessageUtils.ERROR_FAKE);
//        }
//    }

    public Object eKycVerify(EKycRequest eKycRequest) throws IOException {
        BaseResponse vtpocrResponse = new BaseResponse();
        String sessionId = StringUtil.randomString(7);
        ECKycType type = ECKycType.INDEX.get(eKycRequest.getType());
        if (type == null) throw new ECBusinessException("Not support this type");
        Optional<ECDocumentAssignee> assigneeOptional = assigneeRepository.findById(eKycRequest.getAssigneeId());
        if (!assigneeOptional.isPresent()) throw new ECBusinessException("Not found assignee");
        ECDocumentAssignee assignee = assigneeOptional.get();
        ECDocumentSignatureKyc ecDocumentSignatureKyc = ecDocumentSignatureKycRepository.findDocumentSignImage(eKycRequest.getAssigneeId());
        if (ecDocumentSignatureKyc == null) throw new ECBusinessException("Not found assignee kyc");
        switch (type) {
            case FRONT:
                String file = ecDocumentSignatureKyc.getFrontImage();
                if (!Strings.isNullOrEmpty(file)) {
                    File f = new File(String.format("%s%s", upload, file));
                    OCRResponse result = vvnocrService.ocr(sessionId, FileUtils.readFileToByteArray(f), f.getName(), assignee.getDocumentId(), ECKycLogType.OCR.getValue(), assignee.getCompanyId());
                    checkValidateOcr(result, vtpocrResponse);
                    if (vtpocrResponse.getCode() != 1) {
                        throw new ECBusinessException(vtpocrResponse.getMessage());
                    }
                    return new OCRFrontResponseDto().getOCRFrontResponseDto(result);
                }
                break;
            case BACK:
                file = ecDocumentSignatureKyc.getBackImage();
                if (!Strings.isNullOrEmpty(file)) {
                    File f = new File(String.format("%s%s", upload, file));
                    OCRResponse result = vvnocrService.ocr(sessionId, FileUtils.readFileToByteArray(f), f.getName(), assignee.getDocumentId(), ECKycLogType.OCR.getValue(), assignee.getCompanyId());
                    checkValidateOcr(result, vtpocrResponse);
                    if (vtpocrResponse.getCode() != 1) {
                        throw new ECBusinessException(vtpocrResponse.getMessage());
                    }
                    return new OCRBackResponseDto().getOCRBackResponseDto(result);
                }
                break;
            case VERIFY:
                VTPKycResponse vtpKycResponse = new VTPKycResponse();
                file = ecDocumentSignatureKyc.getFrontImage();
                String face = ecDocumentSignatureKyc.getFaceImage();
                if (!Strings.isNullOrEmpty(file) && !Strings.isNullOrEmpty(face)) {
                    File frontImage = new File(String.format("%s%s", upload, file));
                    File faceImage = new File(String.format("%s%s", upload, face));
                    KYCResponse result = vvnocrService.verify(sessionId,
                            FileUtils.readFileToByteArray(frontImage), frontImage.getName(),
                            FileUtils.readFileToByteArray(faceImage), faceImage.getName(), assignee.getDocumentId(), ECKycLogType.VERIFY.getValue(), assignee.getCompanyId());
                    checkVerify(vtpKycResponse, result);
                    if (vtpKycResponse.getCode() != 1) {
                        return vtpKycResponse;
                    }
                    return new KYCResponseDto().getKYCResponseDto(result);
                }
                break;
            default:
                throw new ECBusinessException("eKycVerify does not support this type");
        }

        throw new ECBusinessException("Verify eKYC error");
    }


    public void checkValidateOcr(OCRResponse ocrResponse, BaseResponse vtpocrResponse) {
        switch (ocrResponse.getResultCode()) {
            case 200:
                //psss t??? check th??m
                checkIdCheck(ocrResponse, vtpocrResponse);
                return;
            case 401:
                vtpocrResponse.setCode(401);
                vtpocrResponse.setMessage("Kh??ng c?? quy???n truy c???p eKYC");
                return;
            case 500:
                vtpocrResponse.setCode(500);
                vtpocrResponse.setMessage("H??? th???ng eKYC kh??ng nh???n d???ng ???????c GTTT.");
                return;
            case 501:
                vtpocrResponse.setCode(501);
                vtpocrResponse.setMessage("M??y ch??? eKYC kh??ng ho???t ?????ng");
                return;
            case 402:
                vtpocrResponse.setCode(402);
                vtpocrResponse.setMessage("ID m??? ho???c b??? che");
                return;
            case 201:
                vtpocrResponse.setCode(201);
                vtpocrResponse.setMessage("?????nh d???ng h??? chi???u sai");
                return;
            default:
                throw new ECBusinessException("Result code does not match");
        }
    }

    private void checkIdCheck(OCRResponse ocrResponse, BaseResponse vtpocrResponse) {
        switch (ocrResponse.getIdCheck()) {
            case REAL:
                checkIdLogic(ocrResponse, vtpocrResponse);
                return;
            case FAKE:
                vtpocrResponse.setCode(601);
                vtpocrResponse.setMessage("Gi???y t??? gia?? ma??o");
                return;
            case CONER:
                vtpocrResponse.setCode(601);
                vtpocrResponse.setMessage("???nh b??? m???t g??c");
                return;
            case PUNCH:
                vtpocrResponse.setCode(601);
                vtpocrResponse.setMessage("Gi???y t??? ???? b??? ?????c l???");
                return;
            case BW:
                vtpocrResponse.setCode(601);
                vtpocrResponse.setMessage("Photocopy ??en tr???ng");
                return;
            default:
                throw new ECBusinessException("Id check does not match");
        }
    }

    private void checkIdLogic(OCRResponse ocrResponse, BaseResponse vtpocrResponse) {
        if ("1".equals(ocrResponse.getIdLogic())) {
            checkIsFull(ocrResponse, vtpocrResponse);
        } else {
            vtpocrResponse.setCode(602);
            switch (ocrResponse.getIdLogicMessage()) {
                case ID_IS_EXPIRED:
                    vtpocrResponse.setMessage("Gi???y t??? ???? h???t h???n");
                    return;
                case NOT_MATCH_PROVINCE_CODE:
                    vtpocrResponse.setMessage("Sai m?? t???nh");
                    return;
                case NOT_MATCH_SEX_CODE:
                    vtpocrResponse.setMessage("Gi???i t??nh tr??n gi???y t??? v?? trong s??? ID kh??ng tr??ng kh???p");
                    return;
                case NOT_MATCH_YEAR_CODE:
                    vtpocrResponse.setMessage("N??m sinh tr??n gi???y t??? v?? trong s??? ID kh??ng tr??ng kh???p");
                    return;
                case EXPIRY_SUBTRACT_BIRTHDAY_NOT_GOOD:
                    vtpocrResponse.setMessage("Ng??y th??ng n??m sinh v?? ng??y th??ng h???t h???n kh??ng h???p l??? (?????i v???i CCCD; ng??y h???t h???n l?? khi c??ng d??n ????? 25, 40, 60 tu???i ho???c kh??ng c?? th???i h???n)");
                    return;
                case ID_CAN_BE_FAKE:
                    vtpocrResponse.setMessage("Tr?????ng h???p c?? th??? ??ang b??? gi??? m???o v??? ch??? 'C??N C?????C C??NG D??N/CH???NG MINH NH??N D??N' ?????i v???i gi???y t??? th??? c???ng");
                    return;
                default:
                    throw new ECBusinessException("Id logic does not match");
            }
        }
    }

    private void checkIsFull(OCRResponse ocrResponse, BaseResponse vtpocrResponse) {
        if (ocrResponse.getIsFull().equals("1")) {
            checkIdFull(ocrResponse, vtpocrResponse);
        } else {
            vtpocrResponse.setCode(604);
            vtpocrResponse.setMessage("???nh ch???p b??? m???t g??c ho???c kh??ng ????? th??ng tin");
        }

    }

    private void checkIdFull(OCRResponse ocrResponse, BaseResponse vtpocrResponse) {
        if (ocrResponse.getIdFull().equals("1")) {
            vtpocrResponse.setCode(1);
            vtpocrResponse.setMessage("Success");
        } else {
            //n???u m???t tr?????c
            if (ocrResponse.getIdType().equals("0")) {
                ocrResponse.getListAddressConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6051);
                            vtpocrResponse.setMessage("Th??ng tin ?????a ch??? b??? m???t ho???c m???.");
                        });
                if (vtpocrResponse.getCode() != null) return;

                ocrResponse.getListBirthdayConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6052);
                            vtpocrResponse.setMessage("Th??ng tin ng??y sinh b??? m???t ho???c m???.");
                        });
                if (vtpocrResponse.getCode() != null) return;

                ocrResponse.getListExpiryConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6053);
                            vtpocrResponse.setMessage("Th??ng tin th???i h???n gi???y t??? b??? m???t ho???c m???.");
                        });
                if (vtpocrResponse.getCode() != null) return;

                ocrResponse.getListHometownConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6054);
                            vtpocrResponse.setMessage("Th??ng tin nguy??n qu??n b??? m???t ho???c m???.");
                        });
                if (vtpocrResponse.getCode() != null) return;

                ocrResponse.getListIdConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6055);
                            vtpocrResponse.setMessage("Th??ng tin s??? CCCD/CMT b??? m???t ho???c m???.");
                        });
                if (vtpocrResponse.getCode() != null) return;

                ocrResponse.getListNameConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6056);
                            vtpocrResponse.setMessage("Th??ng tin t??n b??? m???t ho???c m???.");
                        });
                if (vtpocrResponse.getCode() != null) return;

                ocrResponse.getListSexConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6057);
                            vtpocrResponse.setMessage("Th??ng tin gi???i t??nh b??? m???t ho???c m???.");
                        });
                if (vtpocrResponse.getCode() != null) return;
            }
            //n???u m???t sau
            else {

                ocrResponse.getListIssueByConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6058);
                            vtpocrResponse.setMessage("Th??ng tin n??i c???p b??? m???t ho???c m???.");
                        });
                if (vtpocrResponse.getCode() != null) return;

                ocrResponse.getListIssueDateConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6059);
                            vtpocrResponse.setMessage("Th??ng tin ng??y c???p b??? m???t ho???c m???.");
                        });
                if (vtpocrResponse.getCode() != null) return;

                ocrResponse.getListCharacteristicsConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6050);
                            vtpocrResponse.setMessage("Th??ng tin ?????c ??i???m nh???n d???ng b??? m???t ho???c m???.");
                        });
            }
        }
    }

    public void checkVerify(VTPKycResponse kycResponse, KYCResponse response) {
        if (response.getResultCode() != 200) {
            kycResponse.setCode(607);
            if (response.getMessage().getErrorMessage() != null && !response.getMessage().getErrorMessage().equals("")) {
                kycResponse.setMessage(response.getMessage().getErrorMessage());
            } else {
                kycResponse.setMessage("L???i h??? th???ng eKYC");
            }
            return;
        }
        if (!REAL.equals(response.getFaceAntiSpoofStatus().getStatus())) {
            switch (response.getFaceAntiSpoofStatus().getFakeType()) {
                case SCREEN:
                    kycResponse.setCode(6061);
                    kycResponse.setMessage("???nh ch???p l???i t??? m??n h??nh");
                    return;
                case RANDOM_POSE:
                    kycResponse.setCode(6062);
                    kycResponse.setMessage("???nh gi??? m???o do ki???m tra v???i 3 ???nh ch???p quay c??c h?????ng");
                    return;
                case STRAIGHT_POSE:
                    kycResponse.setCode(6063);
                    kycResponse.setMessage("???nh gi??? m???o do ki???m tra 3 ???nh ch???p th???ng li??n ti???p");
                    return;
                default:
                    throw new ECBusinessException("Fake type does not match");
            }
        }
        kycResponse.setCode(1);
        kycResponse.setMessage("Success");
    }

    private static final String REAL = "REAL";
    private static final String FAKE = "FAKE";
    private static final String CONER = "CONER";
    private static final String PUNCH = "PUNCH";
    private static final String BW = "BW";
    private static final String ID_IS_EXPIRED = "ID is expired";
    private static final String NOT_MATCH_PROVINCE_CODE = "Not match province code";
    private static final String NOT_MATCH_SEX_CODE = "Not match sex code";
    private static final String NOT_MATCH_YEAR_CODE = "Not match year code";
    private static final String EXPIRY_SUBTRACT_BIRTHDAY_NOT_GOOD = "Expiry subtract birthday not good";
    private static final String ID_CAN_BE_FAKE = "ID can be fake";
    private static final String CCCD = "CCCD";
    private static final String NEW_ID = "NEW ID";
    private static final String OLD_ID = "OLD ID";
    private static final String PASSPORT = "PASSPORT";
    private static final String DRIVER_LICENSE_OLD = "DRIVER LICENSE OLD";
    private static final String DRIVER_LICENSE_PET = "DRIVER LICENSE PET";
    private static final String CHIP_ID = "CHIP ID";
    private static final String POLICE_ID = "POLICE ID";
    private static final String ARMY_ID = "ARMY ID";
    private static final String SCREEN = "SCREEN";
    private static final String RANDOM_POSE = "RANDOM_POSE";
    private static final String STRAIGHT_POSE = "STRAIGHT_POSE";
    private static final String SUCCESS = "000";
}
