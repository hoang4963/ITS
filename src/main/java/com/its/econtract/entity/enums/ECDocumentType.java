package com.its.econtract.entity.enums;


import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.its.econtract.controllers.request.type.PersistentEnum;


public enum ECDocumentType implements PersistentEnum<Integer> {
    Nhap(1),
    ChoDuyet(2),
    ChoKySo(3),
	TuChoi(4),
	QuaHan(5),
	HuyBo(6),
	ChuaXacThuc(7),
	HoanThanh(8),
	SendTruc(9),
    SendTrucFailed(10),
    HetHan(11);

    private int type;

    ECDocumentType(int type) {
        this.type = type;
    }

    public static final Map<Integer, ECDocumentType> INDEX = Arrays.stream(ECDocumentType.values()).collect(Collectors.toMap(ECDocumentType::getValue, e -> e));

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

    public static ECDocumentType getDocumentState(int type) {
        return INDEX.get(type);
    }

    public static boolean verifySendTruc(ECDocumentType state) {
        return (state == ECDocumentType.HoanThanh || state == ECDocumentType.HuyBo || state == ECDocumentType.QuaHan);
    }

    public boolean isAllowToComplete() {
        return true;
    }
}
