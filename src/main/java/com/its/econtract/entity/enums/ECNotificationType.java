package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ECNotificationType implements PersistentEnum<Integer> {
    ALL(3),
    EMAIL(1),
    SMS(2),
    NO_NOTIFY(0);


    private int type;

    ECNotificationType(int type) {
        this.type = type;
    }

    public static final Map<Integer, ECNotificationType> INDEX = Arrays.stream(ECNotificationType.values()).collect(Collectors.toMap(ECNotificationType::getValue, e -> e));

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
