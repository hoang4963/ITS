package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ECAssigneeType implements PersistentEnum<Byte> {
    CREATOR(0),
    APPROVER(1),
    SIGNATURE(2),
    VIEWER(3);

    private int type;

    ECAssigneeType(int type) {
        this.type = type;
    }

    public static final Map<Byte, ECAssigneeType> INDEX = Arrays.stream(ECAssigneeType.values()).collect(Collectors.toMap(ECAssigneeType::getValue, e -> e));


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
