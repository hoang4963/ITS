package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ECDataType implements PersistentEnum<Byte> {
    E_TEXT_INPUT(1, "Text info"),
    E_CHECK_BOX(3, "Check box"),
    E_RADIO_BOX(4, "Radio");
    private int value;
    private String label;

    ECDataType(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static final Map<Byte, ECDataType> INDEX = Arrays.stream(ECDataType.values()).collect(Collectors.toMap(ECDataType::getValue, e -> e));

    @Override
    public Byte getValue() {
        return (byte)value;
    }

    @Override
    public String getDisplayName() {
        return label;
    }

    @Override
    public Map<Byte, ? extends PersistentEnum<Byte>> getAll() {
        return INDEX;
    }

    public static boolean isText(int i) {
        ECDataType d = INDEX.get((byte)i);
        return d == E_TEXT_INPUT;
    }

    public static ECDataType getDataType(int i) {
        ECDataType d = INDEX.get((byte)i);
        return d;
    }
}
