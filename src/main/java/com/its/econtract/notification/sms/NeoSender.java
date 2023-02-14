package com.its.econtract.notification.sms;

import com.its.econtract.notification.IBuilder;

import java.util.Map;

public class NeoSender implements ISmsSender {

    private NeoSenderBuilder builder;

    private NeoSender (NeoSenderBuilder builder) {
        this.builder = builder;
    }

    @Override
    public Object sendSms() {
        return null;
    }

    public static class NeoSenderBuilder implements IBuilder {
        protected String url;
        protected String username;
        protected String password;
        protected String branchName;
        protected Map<String, String> content;

        public NeoSenderBuilder withBranchName(String branchName) {
            this.branchName = branchName;
            return this;
        }

        public NeoSenderBuilder withUrl(String url) {
            this.url = url;
            return this;
        }

        public NeoSenderBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public NeoSenderBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public NeoSenderBuilder withContent(Map<String, String> content) {
            this.content = content;
            return this;
        }

        @Override
        public NeoSender build() {
            return new NeoSender(this);
        }
    }
}
