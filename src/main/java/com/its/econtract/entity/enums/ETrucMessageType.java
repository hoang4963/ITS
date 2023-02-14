package com.its.econtract.entity.enums;

import com.its.econtract.controllers.request.type.PersistentEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ETrucMessageType implements PersistentEnum<Integer> {
    E_VERIFY_INFO_CA_TRUC(1, "Thông điệp CeCA gửi sang Trục"),
    E_VERIFY_INFO_TRUC_CA(2, "Thông điệp Trục phản hồi đã nhận yêu cầu CeCA"),
    E_VERIFY_SEND_RESULT_TRUC_CA(3, "Thông điệp Trục gửi kết quả cho CeCA"),
    E_VERIFY_SEND_RESULT_CA_TRUC(4, "Thông điệp CeCA phản hồi nhận kết quả Trục"),
    E_VERIFY_SEND_STATE_CA_TRUC(5, "Thông điệp CeCA gửi tới Trục kiểm tra trạng thái của request"),
    E_VERIFY_SEND_STATE_TRUC_CA(6, "Thông điệp Trục phản hồi kết quả kiểm tra gửi tới CeCA"),
    E_VERIFY_GET_PUB_CA_TRUC(7, "Thông điệp CeCA gửi sang Trục lấy chứng thư số"),
    E_VERIFY_GET_PUB_TRUC_CA(8, "Thông điệp Trục gửi trả chứng thư số cho CeCA"),
    E_VERIFY_E_VERIFY_CA_TRUC(9, "Thông điệp CeCA gửi sang Trục xác nhận chứng thư số và dữ liệu gửi Trục xác thực"),
    E_VERIFY_E_VERIFY_TRUC_CA(10, "Thông điệp CeCA gửi sang Trục xác nhận chứng thư số và dữ liệu gửi Trục xác thực");

    private int value;
    private String displayValue;

    public static final Map<Integer, ETrucMessageType> INDEXES = Arrays.stream(ETrucMessageType.values()).collect(Collectors.toMap(ETrucMessageType::getValue, e -> e));

    ETrucMessageType(int value, String displayValue) {
        this.value = value;
        this.displayValue = displayValue;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getDisplayName() {
        return displayValue;
    }

    @Override
    public Map<Integer, ? extends PersistentEnum<Integer>> getAll() {
        return INDEXES;
    }
}
