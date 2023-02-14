package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ETrucSignatureType implements PersistentEnum<Byte> {
    E_VERIFY_SIGNATURE_TYPE_INTERNAL((byte) 1, "Chữ ký nội bộ"),
    E_VERIFY_SIGNATURE_TYPE_REPRESENTATIVE((byte) 2, "Chữ ký đại diện"),
    E_VERIFY_SIGNATURE_TYPE_COMPANY((byte) 3, "Chữ ký con dấu công ty");

    public static final Map<Byte, ETrucSignatureType> INDEXES = Arrays.stream(ETrucSignatureType.values()).collect(Collectors.toMap(ETrucSignatureType::getValue, e -> e));
    private byte value;
    private String display;

    ETrucSignatureType(byte value, String display) {

        this.value = value;
        this.display = display;
    }

    @Override
    public Byte getValue() {
        return value;
    }

    @Override
    public String getDisplayName() {
        return display;
    }

    @Override
    public Map<Byte, ? extends PersistentEnum<Byte>> getAll() {
        return INDEXES;
    }
}
