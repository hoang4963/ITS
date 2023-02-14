/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.its.econtract.controllers.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author quangdt
 */
@Log4j2
@Setter
@ToString
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OCRResponse {

    @JsonProperty("message")
    private String message = "";

    @JsonProperty("result_code")
    private Integer resultCode;

    @JsonProperty("server_name")
    private String serverName;

    @JsonProperty("server_ver")
    private String serverVersion;

    @JsonProperty("document")
    private String document;

    @JsonProperty("id")
    private String id;

    @JsonProperty("idconf")
    private String idConf;

    @JsonProperty("id_logic")
    private String idLogic;

    @JsonProperty("id_logic_message")
    private String idLogicMessage;

    @JsonProperty("id_check")
    private String idCheck;

    @JsonProperty("id_full")
    private String idFull;

    @JsonProperty("name")
    private String name;

    @JsonProperty("nameconf")
    private String nameConf;

    @JsonProperty("birthday")
    private String birthday;

    @JsonProperty("birthdayconf")
    private String birthdayConf;

    @JsonProperty("sex")
    private String sex;

    @JsonProperty("sexconf")
    private String sexConf;

    @JsonProperty("address")
    private String address;

    @JsonProperty("addressconf")
    private String addressConf;

    @JsonProperty("province")
    private String province;

    @JsonProperty("district")
    private String district;

    @JsonProperty("precinct")
    private String precinct;

    @JsonProperty("expiry")
    private String expiry;

    @JsonProperty("expiryconf")
    private String expiryConf;

    @JsonProperty("id_type")
    private String idType;

    @JsonProperty("ethnicity")
    private String ethnicity;

    @JsonProperty("ethnicityconf")
    private String ethnicityConf;

    @JsonProperty("religion")
    private String religion;

    @JsonProperty("religionconf")
    private String religionConf;

    @JsonProperty("issue_date")
    private String issueDate;

    @JsonProperty("issue_date_conf")
    private String issueDateConf;

    @JsonProperty("issue_by")
    private String issueBy;

    @JsonProperty("issue_by_conf")
    private String issueByConf;

    @JsonProperty("country")
    private String country;//Neu la passpord

    @JsonProperty("national")
    private String national;//Neu la passpord hoac bang lai xe

    @JsonProperty("class")
    private String driverClass;

    @JsonProperty("optional_data")
    private String optionalData;

    @JsonProperty("passport_type")
    private String passportType;

    @JsonProperty("street")
    private String street;

    @JsonProperty("street_name")
    private String streetName;

    @JsonProperty("characteristics")
    private String characteristics;

    @JsonProperty("characteristics_conf")
    private String characteristicsConf;

    @JsonProperty("hometown")
    private String homeTown;

    @JsonProperty("hometownconf")
    private String homeTownConf;

    @JsonProperty("copyright")
    private String copyright;

    @JsonProperty("is_full")
    private String isFull;

    @JsonProperty("is_full_score")
    private String isFullScore;

    @JsonProperty("request_id")
    private String requestId;

    public String getAreaCode() {
        return getProvince() + getDistrict() + getPrecinct() + getStreet();
    }

    public String getDistrictCode() {
        return getProvince() + getDistrict();
    }

    public String getPrecintCode() {
        return getProvince() + getDistrict() + getPrecinct();
    }

    public String getMessage() {
        return message;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public String getDocument() {
        return document;
    }

    public String getId() {
        return id;
    }

    public String getIdLogic() {
        return idLogic;
    }

    public String getIdLogicMessage() {
        return idLogicMessage;
    }

    public String getIdCheck() {
        return idCheck;
    }

    public String getName() {
        return name;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getSex() {
        return sex;
    }

    public String getAddress() {
        return address;
    }

    public String getProvince() {
        if (null == province) {
            return "";
        }
        if (N_A.equalsIgnoreCase(province)) {
            return "";
        }
        return province;
    }

    public String getDistrict() {
        if (null == district) {
            return "";
        }
        if (N_A.equalsIgnoreCase(district)) {
            return "";
        }
        return district;
    }

    public String getPrecinct() {
        if (null == precinct) {
            return "";
        }
        if (N_A.equalsIgnoreCase(precinct)) {
            return "";
        }
        return precinct;
    }

    public String getExpiry() {
        return expiry;
    }

    public String getIdType() {
        if (null == idType) {
            return "";
        }
        if (N_A.equalsIgnoreCase(idType)) {
            return "";
        }
        return idType;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public String getReligion() {
        if (null == religion) {
            return "";
        }
        if (N_A.equalsIgnoreCase(religion)) {
            return "";
        }
        return religion;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public String getIssueBy() {
        return issueBy;
    }

    public String getCountry() {
        return country;
    }

    public String getNational() {
        if (null == national) {
            return "";
        }
        if (N_A.equalsIgnoreCase(national)) {
            return "";
        }
        return national;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public String getStreet() {
        if (null == street) {
            return "";
        }
        if (N_A.equalsIgnoreCase(street)) {
            return "N/A";
        }
        return street;
    }

    public String getStreetName() {
        if (null == streetName) {
            return "";
        }
        if (N_A.equalsIgnoreCase(streetName)) {
            return "";
        }
        return streetName;
    }

    public String getIdConf() {
        return idConf;
    }

    public String getNameConf() {
        return nameConf;
    }

    public String getIssueDateConf() {
        return issueDateConf;
    }

    public String getIssueByConf() {
        return issueByConf;
    }

    public String getCharacteristics() {
        return characteristics;
    }

    public String getCharacteristicsConf() {
        return characteristicsConf;
    }

    public String getHometown() {
        return homeTown;
    }

    public List<Float> getListAddressConf() {
        return convert(addressConf);
    }

    public List<Float> getListBirthdayConf() {
        return convert(birthdayConf);
    }

    public List<Float> getListExpiryConf() {
        return convert(expiryConf);
    }

    public List<Float> getListHometownConf() {
        return convert(homeTownConf);
    }

    public List<Float> getListIdConf() {
        return convert(idConf);
    }

    public List<Float> getListNameConf() {
        return convert(nameConf);
    }

    public List<Float> getListSexConf() {
        return convert(sexConf);
    }

    public List<Float> getListIssueByConf() {
        return convert(issueByConf);
    }

    public List<Float> getListIssueDateConf() {
        return convert(issueDateConf);
    }

    public List<Float> getListCharacteristicsConf() {
        return convert(characteristicsConf);
    }

    private List<Float> convert(String data) {
        log.info("Deserialize: {}", data);
        if (data == null || data.isEmpty() || "N/A".equalsIgnoreCase(data)) {
            log.info("Return empty list");
            return new ArrayList<>(0);
        }
        data = data.replace("[", "").replace("]", "").replace(" ", "");

        String[] temps = data.split(",");
        List<Float> lst = new ArrayList<>(temps.length);
        for (String temp : temps) {
            Float f = Float.valueOf(temp);
            lst.add(f);
        }
        log.info("Return list: {}", lst);
        return lst;
    }

    public static final String N_A = "N/A";
    public static final String ID_TYPE_FRONT = "0";
    public static final String ID_TYPE_BACK = "1";
    public static final String DOC_TYPE_CCCD = "CCCD";
    public static final String DOC_TYPE_NEW_ID = "NEW ID";
    public static final String DOC_TYPE_OLD_ID = "OLD ID";
    public static final String DOC_TYPE_PASSPORT = "PASSPORT";
    public static final String DOC_TYPE_DRIVER_LICENSE = "DRIVER LICENSE";
}
