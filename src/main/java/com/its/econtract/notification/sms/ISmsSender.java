package com.its.econtract.notification.sms;

public interface ISmsSender<T> {
    T sendSms();
}
