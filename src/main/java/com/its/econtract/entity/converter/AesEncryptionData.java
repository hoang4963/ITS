package com.its.econtract.entity.converter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class AesEncryptionData {
    public String iv;
    public String value;
    public String mac;
}
