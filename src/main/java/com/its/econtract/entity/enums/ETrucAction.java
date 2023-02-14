package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ETrucAction implements PersistentEnum<Byte> {
    EC_TRUC_ACTION_EDIT_CONTRACT((byte) 1, "Tạo hoặc chỉnh sửa hợp đồng"),
    EC_TRUC_ACTION_CREATE_CONTRACT((byte) 2, "Tạo phụ lục hợp đồng"),
    EC_TRUC_ACTION_CANCEL_CONTRACT((byte) 3, "Hủy văn bản liên quan hoặc hủy cả hợp đồng"),
    EC_TRUC_ACTION_APPR_CONTRACT((byte) 4, "Nghiệm thu hợp đồng"),
    EC_TRUC_ACTION_APPR_SCHEDULE_CONTRACT((byte) 5, "Nghiệm thu định kỳ hợp đồng"),
    EC_TRUC_ACTION_RELEASE_CONTRACT((byte) 6, "Nghiệm thu định kỳ hợp đồng"),
    EC_TRUC_ACTION_APPR_RELEASE_CONTRACT((byte) 7, "Nghiệm thu và thanh lý hợp đồng");

    private byte value;
    private String display;

    public static final Map<Byte, ETrucAction> INDEXES = Arrays.stream(ETrucAction.values()).collect(Collectors.toMap(ETrucAction::getValue, e -> e));

    ETrucAction(byte value, String display) {
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
