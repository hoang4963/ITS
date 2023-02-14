package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ETrucFeedbackState implements PersistentEnum<Byte> {
    E_TRUC_FEEDBACK_STATE_OPEN((byte) 0, "Open"),
    E_TRUC_FEEDBACK_STATE_PROCESSING((byte) 1, "In-Progressing"),
    E_TRUC_FEEDBACK_STATE_SUCCESS((byte) 2, "Success"),
    E_TRUC_FEEDBACK_STATE_FAILED((byte) 3, "Failed");

    public static final Map<Byte, ETrucFeedbackState> INDEXES = Arrays.stream(ETrucFeedbackState.values()).collect(Collectors.toMap(ETrucFeedbackState::getValue, e -> e));
    private byte value;
    private String display;

    ETrucFeedbackState(byte value, String display) {
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
