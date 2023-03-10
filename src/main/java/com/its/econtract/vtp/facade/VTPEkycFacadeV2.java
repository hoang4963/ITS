package com.its.econtract.vtp.facade;

import com.google.common.base.Strings;
import com.its.econtract.controllers.request.EKycRequest;
import com.its.econtract.controllers.response.KYCResponse;
import com.its.econtract.controllers.response.OCRResponse;
import com.its.econtract.entity.ECDocumentAssignee;
import com.its.econtract.entity.ECDocumentSignatureKyc;
import com.its.econtract.entity.VtpCustomerInfo;
import com.its.econtract.entity.enums.ECKycLogType;
import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.utils.ECDateUtils;
import com.its.econtract.repository.ECDocumentAssigneeRepository;
import com.its.econtract.repository.ECDocumentSignatureKycRepository;
import com.its.econtract.repository.VtpCustomerInfoRepository;
import com.its.econtract.utils.MessageUtils;
import com.its.econtract.utils.StringUtil;
import com.its.econtract.vtp.request.*;
import com.its.econtract.vtp.response.BaseResponse;
import com.its.econtract.vtp.response.VTPKycResponse;
import com.its.econtract.vtp.response.VTPOCRResponse;
import com.its.econtract.vtp.service.VTPOCRServiceV2;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Log4j2
@Component
public class VTPEkycFacadeV2 {
    @Autowired
    private VTPOCRServiceV2 vtpocrService;

    @Value(value = "${ai.key}")
    private String key;

    @Autowired
    private VtpCustomerInfoRepository vtpCustomerInfoRepository;

    @Autowired
    private ECDocumentSignatureKycRepository ecDocumentSignatureKycRepository;

    @Value("${file.upload-dir}")
    private String upload;

    @Autowired
    private MessageUtils messageUtil;

    @Value(value = "${confident.limit}")
    float confidentLimit;

    @Autowired
    private ECDocumentAssigneeRepository ecDocumentAssigneeRepository;

    SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmss");

    @SneakyThrows
    public BaseResponse checkImage(CheckImageRequest checkImageRequest) {
        Date requestTime = ECDateUtils.currentTime();
        BaseResponse baseResponse = new BaseResponse();
        String requestId = getRequestId();

        //goi vvn
        OCRResponse response = vtpocrService.ocr(key,
                new OCRRequestV2(checkImageRequest.getIdImage(), requestId),
                checkImageRequest.getCustomerId(), ECKycLogType.CHECK.getValue(), checkImageRequest.getCompanyId());

        //check anh
        checkValidateOcr(response, baseResponse);

        //save db
        Date responseTime = ECDateUtils.currentTime();
        baseResponse.setRequestId(requestId);
        baseResponse.setRequestTime(String.valueOf(ECDateUtils.format(ECDateUtils.YYYYMMDDHHMMSS_SLASH, requestTime)));
        baseResponse.setResponseTime(String.valueOf(ECDateUtils.format(ECDateUtils.YYYYMMDDHHMMSS_SLASH, responseTime)));
        return baseResponse;
    }


    @SneakyThrows
    public VTPOCRResponse ocr(ECOCRRequest request) {
        Date requestTime = ECDateUtils.currentTime();
        VTPOCRResponse vtpocrResponse = new VTPOCRResponse();
        VTPOCRResponse.Information information = new VTPOCRResponse.Information();
        VtpCustomerInfo vtpCustomerInfo = vtpCustomerInfoRepository.findCustomerImage(request.getCustomerId());
        if (vtpCustomerInfo == null) throw new ECBusinessException("Not found customer info");
        String requestId = getRequestId();
        // Front image
        OCRResponse ocrResponseFront = vtpocrService.ocr(key,
                new OCRRequestV2(convertFileToBase64(vtpCustomerInfo.getFrontImage()), requestId),
                request.getCustomerId(), ECKycLogType.OCR.getValue(), request.getCompanyId());
        // check m???t tr?????c
        checkValidateOcr(ocrResponseFront, vtpocrResponse);
        // build information front
        information = buildInformationOcrFront(information, ocrResponseFront);

        // Back image
        OCRResponse ocrResponseBack = vtpocrService.ocr(key,
                new OCRRequestV2(convertFileToBase64(vtpCustomerInfo.getBackImage()), requestId),
                request.getCustomerId(), ECKycLogType.OCR.getValue(), request.getCompanyId());
        // check m???t sau
        checkValidateOcr(ocrResponseBack, vtpocrResponse);
        // check back
        if (ocrResponseFront.getIdType().equals(ocrResponseBack.getIdType())) {
            vtpocrResponse.setCode(603);
            vtpocrResponse.setMessage("Kh??ng ????? ???nh 2 m???t gi???y t???");
        }
        // check 2 image spoof
        spoofCheckTwoImage(ocrResponseFront, ocrResponseBack, vtpocrResponse);
        // build information back
        information = buildInformationOcrBack(information, ocrResponseBack);
        // save db

        // set time and request id
        Date responseTime = ECDateUtils.currentTime();
        vtpocrResponse.setRequestId(requestId);
        vtpocrResponse.setRequestTime(String.valueOf(ECDateUtils.format(ECDateUtils.YYYYMMDDHHMMSS_SLASH, requestTime)));
        vtpocrResponse.setResponseTime(String.valueOf(ECDateUtils.format(ECDateUtils.YYYYMMDDHHMMSS_SLASH, responseTime)));
        if (vtpocrResponse.getCode() != 1) {
            return vtpocrResponse;
        }
        vtpocrResponse.setInformation(information);
        return vtpocrResponse;
    }

    private void checkValidateOcr(OCRResponse ocrResponse, BaseResponse vtpocrResponse) {
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

    private void spoofCheckTwoImage(OCRResponse ocrResponseFront, OCRResponse ocrResponseBack, VTPOCRResponse vtpocrResponse) {
        //if ko c??ng doc b???n ra l???i
//        if (!ocrResponseFront.getDocument().equals(ocrResponseBack.getDocument())) {
//            vtpocrResponse.setCode(606);
//            vtpocrResponse.setMessage("C???p ???nh kh??ng ph???i c???a c??ng 1 lo???i GTTT.");
//            return;
//        }

        if (ocrResponseFront.getIdType().equals(ocrResponseBack.getIdType())) {
            vtpocrResponse.setCode(606);
            vtpocrResponse.setMessage("Kh??ng ????? 2 m???t c???a GTTT.");
            return;
        }

        //....2 ???nh kh??c m???t ko ph???i c???a 1 GTTT kh?? check
        switch (ocrResponseBack.getDocument()) {
//                confirm
//                case CCCD:
//                    return;
//                case NEW_ID:
//                    return;
//                case OLD_ID:
//                    return;
//            case CHIP_ID:
//                if (ocrResponseFront.getId().equals(ocrResponseBack.getId())) {
//                    vtpocrResponse.setCode(1);
//                    vtpocrResponse.setMessage("Success");
//                } else {
//                    vtpocrResponse.setCode(606);
//                    vtpocrResponse.setMessage("C???p ???nh kh??ng c??ng 1 gi???y t???.");
//                }
//                return;
            default:
                vtpocrResponse.setCode(1);
                vtpocrResponse.setMessage("Success");
        }
    }

    @SneakyThrows
    public VTPKycResponse verify(EKycRequest kycRequest) {
        Date requestTime = ECDateUtils.currentTime();
        VTPKycResponse vtpKycResponse = new VTPKycResponse();
        String requestId = getRequestId();
        ECDocumentSignatureKyc ecDocumentSignatureKyc = ecDocumentSignatureKycRepository.findDocumentSignImage(kycRequest.getAssigneeId());
        if (ecDocumentSignatureKyc == null) throw new ECBusinessException("Not found assignee kyc");
        Optional<ECDocumentAssignee> assignee = ecDocumentAssigneeRepository.findById(kycRequest.getAssigneeId());
        if (!assignee.isPresent()) throw new ECBusinessException("Not found assignee");
        KYCResponse kycResponse = vtpocrService.verify(key, new KYCRequestV2(
                        convertFileToBase64(ecDocumentSignatureKyc.getFrontImage()),
                        convertFileToBase64(ecDocumentSignatureKyc.getFaceImage()), requestId),
                assignee.get().getDocumentId(), ECKycLogType.VERIFY.getValue(), assignee.get().getCompanyId());
        checkVerify(vtpKycResponse, kycResponse);
        //save db
        Date responseTime = ECDateUtils.currentTime();
        vtpKycResponse.setRequestId(requestId);
        vtpKycResponse.setRequestTime(String.valueOf(ECDateUtils.format(ECDateUtils.YYYYMMDDHHMMSS_SLASH, requestTime)));
        vtpKycResponse.setResponseTime(String.valueOf(ECDateUtils.format(ECDateUtils.YYYYMMDDHHMMSS_SLASH, responseTime)));

        if (vtpKycResponse.getCode() != 1) {
            return vtpKycResponse;
        }
        vtpKycResponse.setScore(kycResponse.getSim());
        vtpKycResponse.setVerifyResult(kycResponse.getSim() > 0.63);
        return vtpKycResponse;
    }

    private void checkVerify(VTPKycResponse kycResponse, KYCResponse response) {
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

    private String convertFileToBase64(String f1) throws IOException {
        if (Strings.isNullOrEmpty(f1)) throw new ECBusinessException("Not found image resource");
        File f = new File(String.format("%s%s", upload, f1));
        byte[] fileContent = FileUtils.readFileToByteArray(f);
        String file = Base64.getEncoder().encodeToString(fileContent);
        return file;
    }

    private VTPOCRResponse.Information buildInformationOcrFront(VTPOCRResponse.Information information, OCRResponse ocrResponse) {
        if (information == null) {
            information = new VTPOCRResponse.Information();
        }
        information.setId(ocrResponse.getId());
        information.setName(ocrResponse.getName());
        information.setBirthday(ocrResponse.getBirthday());
        information.setBirthplace(ocrResponse.getHometown());
        information.setSex(ocrResponse.getSex());
        information.setAddress(ocrResponse.getAddress());
        information.setProvince("");
        information.setDistrict("");
        information.setWard("");
        information.setProvinceCode(ocrResponse.getProvince());
        information.setDistrictCode(ocrResponse.getDistrict());
        information.setWardCode(ocrResponse.getPrecinct());
        information.setStreet(ocrResponse.getStreetName());
        information.setNationality(ocrResponse.getNational());
        information.setReligion(ocrResponse.getReligion());
        information.setEthnicity(ocrResponse.getEthnicity());
        information.setExpiry(ocrResponse.getExpiry());
        information.setLicenceClass(ocrResponse.getDriverClass());
        information.setPassportId(ocrResponse.getOptionalData());
        information.setPassportType(ocrResponse.getPassportType());
        information.setMilitaryTitle("");
        information.setTypeBlood("");
        information.setCmndId("");
        information.setTypeList("");
        information.setDocument(ocrResponse.getDocument());
        switch (ocrResponse.getDocument()) {
            case CCCD:
            case CHIP_ID:
                information.setType("CCCD");
                break;
            case NEW_ID:
                information.setType("CMCC");
                break;
            case OLD_ID:
                information.setType("VMND");
                break;
            case PASSPORT:
                information.setType("Passport");
                break;
            case DRIVER_LICENSE_OLD:
            case DRIVER_LICENSE_PET:
                information.setType("BLX");
                break;
            case POLICE_ID:
                information.setType("CMCS");
                break;
            case ARMY_ID:
                information.setType("CMQD");
                break;
            default:
                information.setType("");
        }
        return information;
    }

    private VTPOCRResponse.Information buildInformationOcrBack(VTPOCRResponse.Information information, OCRResponse ocrResponse) {
        if (information == null) {
            information = new VTPOCRResponse.Information();
        }
        information.setFeature(ocrResponse.getCharacteristics());
        information.setIssueBy(ocrResponse.getIssueBy());
        information.setIssueDate(ocrResponse.getIssueDate());
        return information;
    }

    private synchronized String getRequestId() {
        String sessionId = StringUtil.randomString(7);
        return sdf.format(new Date()) + "-" + sessionId + "-" + StringUtil.randomString(7);
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
