package com.its.econtract.aop;

import com.its.econtract.entity.ECDocumentAssignee;
import com.its.econtract.entity.ECDocumentLog;
import com.its.econtract.utils.ECDateUtils;
import com.its.econtract.repository.ECDocumentLogRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class ECSignatureLogMonitor {
    private static final Logger LOGGER = LogManager.getLogger(ECSignatureLogMonitor.class);
    private ExecutorService executor;

    @PostConstruct
    public void initMethod() {
        LOGGER.info("[ECSignatureLogMonitor] initMethod to create executor");
        this.executor = Executors.newFixedThreadPool(1);
    }

    @PreDestroy
    public void killBean() {
        LOGGER.info("[ECSignatureLogMonitor] killBean && executor");
        try {
            this.executor.shutdown();
            this.executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            this.executor.shutdownNow();
        }
    }

    @Autowired
    private ECDocumentLogRepository logRepository;

    @Around("@annotation(ECDocSignLog)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        ECDocSignLog loggableMethod = method.getAnnotation(ECDocSignLog.class);
        //before
        Class clazz = proceedingJoinPoint.getTarget().getClass();
        long startTime = ECDateUtils.currentTimeMillis();
        LOGGER.info("{} execute with method = {} start: {}", clazz, method.getName(), startTime);
        //start method execution
        Object result = proceedingJoinPoint.proceed();
        LOGGER.info("Result = {}", result);
        //show results
        if (result != null) {
            boolean showResults = loggableMethod != null && loggableMethod.logSign();
            if (showResults) {
                LOGGER.info("Begin insert log for ECDocumentLog");
                Object[] params = proceedingJoinPoint.getArgs();
                ECDocumentLog documentLog = new ECDocumentLog();
                documentLog.setDocumentId((int) params[0]);
                ECDocumentAssignee as = (ECDocumentAssignee) params[2];
                documentLog.setContent("Thực hiện ký số tài liệu thành công");
                documentLog.setCreatedAt(new Date());
                documentLog.setShow(true);
                documentLog.setAction(6);
                documentLog.setNextValue("{}");
                documentLog.setPrevValue("{}");
                documentLog.setActionBy(as.getFullName());
                documentLog.setActionByEmail(as.getEmail());
                executor.execute(() -> {
                    logRepository.save(documentLog);
                });
                LOGGER.info("End insert log for ECDocumentLog");
            }
        }
        //show after
        long endTime = System.currentTimeMillis();
        LOGGER.info("{} execute with method = {} end: {} duration: {}", clazz, method.getName(), endTime, endTime - startTime);
        return result;
    }
}
