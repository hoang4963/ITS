package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ETrucSignatureGroup implements PersistentEnum<Byte> {
    E_VERIFY_SIGNATURE_BCT((byte) 1, "Chữ ký của Bộ Công thương"),
    E_VERIFY_SIGNATURE_CA((byte) 2, "Chữ ký của CeCA"),
    E_VERIFY_SIGNATURE_COMPANY_PERSONAL((byte) 3, "Chữ ký của Doanh nghiệp và cá nhân");

    public static final Map<Byte, ETrucSignatureGroup> INDEXES = Arrays.stream(ETrucSignatureGroup.values()).collect(Collectors.toMap(ETrucSignatureGroup::getValue, e -> e));
    private byte value;
    private String display;

    ETrucSignatureGroup(byte value, String display) {

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
