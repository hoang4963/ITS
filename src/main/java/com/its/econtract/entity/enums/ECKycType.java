package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ECKycType implements PersistentEnum<Byte> {
    FRONT((byte)1),
    BACK((byte)2),
    VERIFY((byte)3);

    private byte value;

    ECKycType(byte value) {
        this.value = value;
    }

    public static final Map<Byte, ECKycType> INDEX = Arrays.stream(ECKycType.values()).collect(Collectors.toMap(ECKycType::getValue, e -> e));

    @Override
    public Byte getValue() {
        return value;
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public Map<Byte, ? extends PersistentEnum<Byte>> getAll() {
        return INDEX;
    }
}
