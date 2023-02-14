package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ECKycLogType implements PersistentEnum<Integer> {
    OCR(1),
    VERIFY(0),
    CHECK(2);

    private int type;

    ECKycLogType(int type) {
        this.type = type;
    }

    public static final Map<Integer, ECKycLogType> INDEX = Arrays.stream(ECKycLogType.values()).collect(Collectors.toMap(ECKycLogType::getValue, e -> e));


    @Override
    public Integer getValue() {
        return (Integer)this.type;
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public Map<Integer, ? extends PersistentEnum<Integer>> getAll() {
        return INDEX;
    }
}
