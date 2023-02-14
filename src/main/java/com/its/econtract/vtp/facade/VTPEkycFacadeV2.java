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
        // check mặt trước
        checkValidateOcr(ocrResponseFront, vtpocrResponse);
        // build information front
        information = buildInformationOcrFront(information, ocrResponseFront);

        // Back image
        OCRResponse ocrResponseBack = vtpocrService.ocr(key,
                new OCRRequestV2(convertFileToBase64(vtpCustomerInfo.getBackImage()), requestId),
                request.getCustomerId(), ECKycLogType.OCR.getValue(), request.getCompanyId());
        // check mặt sau
        checkValidateOcr(ocrResponseBack, vtpocrResponse);
        // check back
        if (ocrResponseFront.getIdType().equals(ocrResponseBack.getIdType())) {
            vtpocrResponse.setCode(603);
            vtpocrResponse.setMessage("Không đủ ảnh 2 mặt giấy tờ");
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
                //psss tự check thêm
                checkIdCheck(ocrResponse, vtpocrResponse);
                return;
            case 401:
                vtpocrResponse.setCode(401);
                vtpocrResponse.setMessage("Không có quyền truy cập eKYC");
                return;
            case 500:
                vtpocrResponse.setCode(500);
                vtpocrResponse.setMessage("Hệ thống eKYC không nhận dạng được GTTT.");
                return;
            case 501:
                vtpocrResponse.setCode(501);
                vtpocrResponse.setMessage("Máy chủ eKYC không hoạt động");
                return;
            case 402:
                vtpocrResponse.setCode(402);
                vtpocrResponse.setMessage("ID mờ hoặc bị che");
                return;
            case 201:
                vtpocrResponse.setCode(201);
                vtpocrResponse.setMessage("Định dạng hộ chiếu sai");
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
                vtpocrResponse.setMessage("Giấy tờ giả mạo");
                return;
            case CONER:
                vtpocrResponse.setCode(601);
                vtpocrResponse.setMessage("Ảnh bị mất góc");
                return;
            case PUNCH:
                vtpocrResponse.setCode(601);
                vtpocrResponse.setMessage("Giấy tờ đã bị đục lỗ");
                return;
            case BW:
                vtpocrResponse.setCode(601);
                vtpocrResponse.setMessage("Photocopy đen trắng");
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
                    vtpocrResponse.setMessage("Giấy tờ đã hết hạn");
                    return;
                case NOT_MATCH_PROVINCE_CODE:
                    vtpocrResponse.setMessage("Sai mã tỉnh");
                    return;
                case NOT_MATCH_SEX_CODE:
                    vtpocrResponse.setMessage("Giới tính trên giấy tờ và trong số ID không trùng khớp");
                    return;
                case NOT_MATCH_YEAR_CODE:
                    vtpocrResponse.setMessage("Năm sinh trên giấy tờ và trong số ID không trùng khớp");
                    return;
                case EXPIRY_SUBTRACT_BIRTHDAY_NOT_GOOD:
                    vtpocrResponse.setMessage("Ngày tháng năm sinh và ngày tháng hết hạn không hợp lệ (đối với CCCD; ngày hết hạn là khi công dân đủ 25, 40, 60 tuổi hoặc không có thời hạn)");
                    return;
                case ID_CAN_BE_FAKE:
                    vtpocrResponse.setMessage("Trường hợp có thể đang bị giả mạo về chữ 'CĂN CƯỚC CÔNG DÂN/CHỨNG MINH NHÂN DÂN' đối với giấy tờ thẻ cứng");
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
            vtpocrResponse.setMessage("Ảnh chụp bị mất góc hoặc không đủ thông tin");
        }

    }

    private void checkIdFull(OCRResponse ocrResponse, BaseResponse vtpocrResponse) {
        if (ocrResponse.getIdFull().equals("1")) {
            vtpocrResponse.setCode(1);
            vtpocrResponse.setMessage("Success");
        } else {
            //nếu mặt trước
            if (ocrResponse.getIdType().equals("0")) {
                ocrResponse.getListAddressConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6051);
                            vtpocrResponse.setMessage("Thông tin địa chỉ bị mất hoặc mờ.");
                        });
                if (vtpocrResponse.getCode() != null) return;

                ocrResponse.getListBirthdayConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6052);
                            vtpocrResponse.setMessage("Thông tin ngày sinh bị mất hoặc mờ.");
                        });
                if (vtpocrResponse.getCode() != null) return;

                ocrResponse.getListExpiryConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6053);
                            vtpocrResponse.setMessage("Thông tin thời hạn giấy tờ bị mất hoặc mờ.");
                        });
                if (vtpocrResponse.getCode() != null) return;

                ocrResponse.getListHometownConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6054);
                            vtpocrResponse.setMessage("Thông tin nguyên quán bị mất hoặc mờ.");
                        });
                if (vtpocrResponse.getCode() != null) return;

                ocrResponse.getListIdConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6055);
                            vtpocrResponse.setMessage("Thông tin số CCCD/CMT bị mất hoặc mờ.");
                        });
                if (vtpocrResponse.getCode() != null) return;

                ocrResponse.getListNameConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6056);
                            vtpocrResponse.setMessage("Thông tin tên bị mất hoặc mờ.");
                        });
                if (vtpocrResponse.getCode() != null) return;

                ocrResponse.getListSexConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6057);
                            vtpocrResponse.setMessage("Thông tin giới tính bị mất hoặc mờ.");
                        });
                if (vtpocrResponse.getCode() != null) return;
            }
            //nếu mặt sau
            else {

                ocrResponse.getListIssueByConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6058);
                            vtpocrResponse.setMessage("Thông tin nơi cấp bị mất hoặc mờ.");
                        });
                if (vtpocrResponse.getCode() != null) return;

                ocrResponse.getListIssueDateConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6059);
                            vtpocrResponse.setMessage("Thông tin ngày cấp bị mất hoặc mờ.");
                        });
                if (vtpocrResponse.getCode() != null) return;

                ocrResponse.getListCharacteristicsConf().stream().
                        filter((f) -> (f != null && confidentLimit > f)).
                        forEachOrdered((i) -> {
                            vtpocrResponse.setCode(6050);
                            vtpocrResponse.setMessage("Thông tin đặc điểm nhận dạng bị mất hoặc mờ.");
                        });
            }
        }
    }

    private void spoofCheckTwoImage(OCRResponse ocrResponseFront, OCRResponse ocrResponseBack, VTPOCRResponse vtpocrResponse) {
        //if ko cùng doc bắn ra lỗi
//        if (!ocrResponseFront.getDocument().equals(ocrResponseBack.getDocument())) {
//            vtpocrResponse.setCode(606);
//            vtpocrResponse.setMessage("Cặp ảnh không phải của cùng 1 loại GTTT.");
//            return;
//        }

        if (ocrResponseFront.getIdType().equals(ocrResponseBack.getIdType())) {
            vtpocrResponse.setCode(606);
            vtpocrResponse.setMessage("Không đủ 2 mặt của GTTT.");
            return;
        }

        //....2 ảnh khác mặt ko phải của 1 GTTT khó check
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
//                    vtpocrResponse.setMessage("Cặp ảnh không cùng 1 giấy tờ.");
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
                kycResponse.setMessage("Lỗi hệ thống eKYC");
            }
            return;
        }
        if (!REAL.equals(response.getFaceAntiSpoofStatus().getStatus())) {
            switch (response.getFaceAntiSpoofStatus().getFakeType()) {
                case SCREEN:
                    kycResponse.setCode(6061);
                    kycResponse.setMessage("Ảnh chụp lại từ màn hình");
                    return;
                case RANDOM_POSE:
                    kycResponse.setCode(6062);
                    kycResponse.setMessage("Ảnh giả mạo do kiểm tra với 3 ảnh chụp quay các hướng");
                    return;
                case STRAIGHT_POSE:
                    kycResponse.setCode(6063);
                    kycResponse.setMessage("Ảnh giả mạo do kiểm tra 3 ảnh chụp thẳng liên tiếp");
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
