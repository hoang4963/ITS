package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ECRemimderType implements PersistentEnum<Integer> {
    NEAR_EXPIRE_LEASING(1),
    EXPIRED_LEASING(2),
    NEAR_EXPIRE(3),
    EXPIRE(4);

    private int type;

    ECRemimderType(int type) {
        this.type = type;
    }

    public static final Map<Integer, ECRemimderType> INDEX = Arrays.stream(ECRemimderType.values()).collect(Collectors.toMap(ECRemimderType::getValue, e -> e));

    @Override
    public Integer getValue() {
        return this.type;
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
