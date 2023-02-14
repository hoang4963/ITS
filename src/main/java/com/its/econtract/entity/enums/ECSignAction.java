package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ECSignAction implements PersistentEnum<Integer> {
    EC_SIGN_ACTION_PROCESSING(0, "Begin signing document"),
    EC_SIGN_ACTION_COMPLETED(1, "Completed signing document"),
    EC_SIGN_ACTION_BTC_CONFIRMED(2, "BTC confirmed");
    int value;
    String displayName;

    private static Map<Integer, ECSignAction> INDEXES = Arrays.stream(ECSignAction.values()).collect(Collectors.toMap(ECSignAction::getValue, e -> e));

    ECSignAction(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Map<Integer, ? extends PersistentEnum<Integer>> getAll() {
        return INDEXES;
    }

    public static ECSignAction getSignAction(int actionType) {
        return INDEXES.get(actionType);
    }
}
