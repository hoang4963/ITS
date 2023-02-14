package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ETrucContractType implements PersistentEnum<Byte> {
    E_VERIFY_CONTRACT_TYPE_QUALIFIED((byte) 1, "Các bên tham gia đều sử dụng chữ ký số"),
    E_VERIFY_CONTRACT_TYPE_ADVANCED((byte) 2, "Kết hợp giữa chữ ký số và eKYC"),
    E_VERIFY_CONTRACT_TYPE_BASIC((byte) 3, "Một bên sử dụng chữ ký số và bảo đảm cho bên còn lại theo quy trình giao kết hợp đồng thỏa thuận"),
    E_VERIFY_CONTRACT_TYPE_PERSONAL((byte) 4, "Hợp đồng giữa 2 cá nhân đều có chữ ký số");

    public static final Map<Byte, ETrucContractType> INDEXES = Arrays.stream(ETrucContractType.values()).collect(Collectors.toMap(ETrucContractType::getValue, e -> e));
    private byte value;
    private String display;

    ETrucContractType(byte value, String display) {

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
