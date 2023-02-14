package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ECPartnerType implements PersistentEnum<Byte> {
    MYCOMPANY(1),
    INDIVIDUAL(3),
    COMPANY(2);

    private int type;

    ECPartnerType(int type) {
        this.type = type;
    }

    public static final Map<Byte, ECPartnerType> INDEX = Arrays.stream(ECPartnerType.values()).collect(Collectors.toMap(ECPartnerType::getValue, e -> e));


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
