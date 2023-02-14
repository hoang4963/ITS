package com.its.econtract.aop;

import com.its.econtract.utils.ECDateUtils;
import com.its.econtract.repository.ECDocumentConversationRepository;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Log4j2
@Aspect
@Component
public class ConversationMonitor {

    @Autowired
    private ECDocumentConversationRepository reposition;

    @Around(value = "@within(com.its.econtract.aop.ConversationLoggable) || @annotation(com.its.econtract.aop.ConversationLoggable)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        ConversationLoggable loggableMethod = method.getAnnotation(ConversationLoggable.class);
        //before
        Class clazz = proceedingJoinPoint.getTarget().getClass();
        long startTime = ECDateUtils.currentTimeMillis();
        log.info("{} execute with method = {} start: {}", clazz, method.getName(), startTime);
        //start method execution
        Object result = proceedingJoinPoint.proceed();
        log.info("Result = {}", result);
        //show results
        if (result != null) {
            boolean showResults = loggableMethod != null && loggableMethod.saveConversation();
            if (showResults) {
                //TODO: need monitor data in this.
            }
        }
        //show after
        long endTime = System.currentTimeMillis();
        log.info("{} execute with method = {} end: {} duration: {}", clazz, method.getName(), endTime, endTime - startTime);
        return result;
    }
}
