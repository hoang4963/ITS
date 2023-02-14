package com.its.econtract.component;

import org.jodconverter.DocumentConverter;
import org.jodconverter.OnlineConverter;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.office.OnlineOfficeManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class ECOnlineOfficeConfiguration {

    @Value("${office.url}")
    private String officeUrl;

    @Value("${file.upload-dir}")
    private String baseDir;

    @Value("${pool_size}")
    private int poolSize;

    private OfficeManager createOfficeManager() {
        final OnlineOfficeManager.Builder builder = OnlineOfficeManager.builder();
        builder.urlConnection(officeUrl);
        builder.poolSize(poolSize);
        builder.workingDir(baseDir);
        builder.taskExecutionTimeout(1000000000);
        builder.taskQueueTimeout(1000000000);
        return builder.build();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean(name = "onlineOfficeManager")
    public OfficeManager onlineOfficeManager() {
        return createOfficeManager();
    }

    @Bean
    @ConditionalOnMissingBean(name = "onlineDocumentConverter")
    @ConditionalOnBean(name = "onlineOfficeManager")
    public DocumentConverter onlineDocumentConverter(final OfficeManager onlineOfficeManager) {
        return OnlineConverter.make(onlineOfficeManager);
    }
}
