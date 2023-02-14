package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ETrucServiceType implements PersistentEnum<String> {
    E_VERIFY_SERVICE_TYPE_CA( "0", "Lấy thông tin chứng thư của BCT"),
    E_VERIFY_SERVICE_TYPE_E_VERIFY( "101", "Xác thực"),
    E_VERIFY_SERVICE_TYPE_TIME_STAMP("102", "Cấp dấu thời gian kèm xác thực"),
    E_VERIFY_SERVICE_TYPE_GET_TIME("2", "Cấp dấu thời gian");

    public static final Map<String, ETrucServiceType> INDEXES = Arrays.stream(ETrucServiceType.values()).collect(Collectors.toMap(ETrucServiceType::getValue, e -> e));
    private String value;
    private String display;

    ETrucServiceType(String value, String display) {
        this.value = value;
        this.display = display;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDisplayName() {
        return display;
    }

    @Override
    public Map<String, ? extends PersistentEnum<String>> getAll() {
        return INDEXES;
    }
}
