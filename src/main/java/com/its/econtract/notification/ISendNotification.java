package com.its.econtract.notification;

import com.its.econtract.repository.ECDocumentConversationRepository;

public interface ISendNotification {
    void setConversationRepository(ECDocumentConversationRepository documentConversationRepository);
    int sendMsg();
    int reSendMsg();
}
