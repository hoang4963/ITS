package com.its.econtract.entity.enums;


import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


public enum ECDocumentState implements PersistentEnum<Integer> {
    Nhap(1),
    ChoDuyet(2),
    ChoKySo(3),
	TuChoi(4),
	QuaHan(5),
	HuyBo(6),
	ChuaXacThuc(7),
	HoanThanh(8);

    private int type;

    ECDocumentState(int type) {
        this.type = type;
    }

    public static final Map<Integer, ECDocumentState> INDEX = Arrays.stream(ECDocumentState.values()).collect(Collectors.toMap(ECDocumentState::getValue, e -> e));

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
