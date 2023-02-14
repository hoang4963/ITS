package com.its.econtract.vtp.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class VTPOCRResponse extends BaseResponse{

    @JsonProperty(value = "information")
    private Information information;

    public VTPOCRResponse(BaseResponse baseResponse, Information information) {
        super.setCode(baseResponse.getCode());
        super.setMessage(baseResponse.getMessage());
        super.setRequestId(baseResponse.getRequestId());
        super.setRequestTime(baseResponse.getRequestTime());
        super.setResponseTime(baseResponse.getResponseTime());
        super.setSignature(baseResponse.getSignature());
        this.information = information;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Information{
        @JsonProperty(value = "id")
        private String id;
        @JsonProperty(value = "name")
        private String name;
        @JsonProperty(value = "birthday")
        private String birthday;
        @JsonProperty(value = "birthplace")
        private String birthplace;
        @JsonProperty(value = "sex")
        private String sex;
        @JsonProperty(value = "address")
        private String address;
        @JsonProperty(value = "province")
        private String province;
        @JsonProperty(value = "district")
        private String district;
        @JsonProperty(value = "ward")
        private String ward;
        @JsonProperty(value = "province_code")
        private String provinceCode;
        @JsonProperty(value = "district_code")
        private String districtCode;
        @JsonProperty(value = "ward_code")
        private String wardCode;
        @JsonProperty(value = "street")
        private String street;
        @JsonProperty(value = "nationality")
        private String nationality;
        @JsonProperty(value = "religion")
        private String religion;
        @JsonProperty(value = "ethnicity")
        private String ethnicity;
        @JsonProperty(value = "expiry")
        private String expiry;
        @JsonProperty(value = "feature")
        private String feature;
        @JsonProperty(value = "issue_date")
        private String issueDate;
        @JsonProperty(value = "issue_by")
        private String issueBy;
        @JsonProperty(value = "licence_class")
        private String licenceClass;
        @JsonProperty(value = "passport_id")
        private String passportId;
        @JsonProperty(value = "passport_type")
        private String passportType;
        @JsonProperty(value = "military_title")
        private String militaryTitle;
        @JsonProperty(value = "type_blood")
        private String typeBlood;
        @JsonProperty(value = "cmnd_id")
        private String cmndId;
        @JsonProperty(value = "type")
        private String type;
        @JsonProperty(value = "type_list")
        private String typeList;
        @JsonProperty(value = "document")
        private String document;
    }
}
