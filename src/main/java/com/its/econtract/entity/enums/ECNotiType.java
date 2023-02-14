package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ECNotiType implements PersistentEnum<Byte> {
    SMS(0),
    EMAIL(1);

    private int type;

    ECNotiType(int type) {
        this.type = type;
    }

    public static final Map<Byte, ECNotiType> INDEX = Arrays.stream(ECNotiType.values()).collect(Collectors.toMap(ECNotiType::getValue, e -> e));

    @Override
    public Byte getValue() {
        return (byte)this.type;
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
