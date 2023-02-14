package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ECSignType implements PersistentEnum<Integer> {
    E_CA_SIGN(0, "USB token, Remote signing CA sign type"),
    E_SMS_SIGN(1, "SMS sign type"),
    E_EKYC_SIGN(2, "eKYC sign type"),
    E_MY_SIGN(3, "My sign type");

    public int value;

    private String displayValue;

    private static Map<Integer, ECSignType> INDEXES = Arrays.stream(ECSignType.values()).collect(Collectors.toMap(ECSignType::getValue, e -> e));

    ECSignType(int value, String displayValue) {
        this.value = value;
        this.displayValue = displayValue;
    }

    public static ECSignType getSignType(int signType) {
        return INDEXES.get(signType);
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getDisplayName() {
        return displayValue;
    }

    @Override
    public Map<Integer, ? extends PersistentEnum<Integer>> getAll() {
        return INDEXES;
    }
}
