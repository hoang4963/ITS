package com.its.econtract.aop;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Component
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface ConversationLoggable {
    boolean saveConversation() default false;
}
