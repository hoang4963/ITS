package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ECDocumentAddendumType implements PersistentEnum<Integer> {
    ADDITIONAL(0),
    EXTEND(1),
    CANCEL(2);

    private int type;

    ECDocumentAddendumType(int type) {
        this.type = type;
    }

    public static final Map<Integer, ECDocumentAddendumType> INDEX = Arrays.stream(ECDocumentAddendumType.values()).collect(Collectors.toMap(ECDocumentAddendumType::getValue, e -> e));


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
