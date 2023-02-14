package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ECOrganisationType implements PersistentEnum<Byte> {
    ORG_OWNER(1),
    ORG_PARTNER(2),
    INDIVIDUAL(3);

    private int type;

    ECOrganisationType(int type) {
        this.type = type;
    }

    public static final Map<Byte, ECOrganisationType> INDEX = Arrays.stream(ECOrganisationType.values()).collect(Collectors.toMap(ECOrganisationType::getValue, e -> e));


    @Override
    public Byte getValue() {
        return (byte)this.type;
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public Map<Byte, ? extends PersistentEnum<Byte>> getAll() {
        return INDEX;
    }

    public static boolean isPersonal(int type) {
        ECOrganisationType or = INDEX.get(type);
        if (or == null) return false;
        return or == ECOrganisationType.INDIVIDUAL;
    }
}
