package com.its.econtract.component;
//Cheat - mvn
import lombok.extern.log4j.Log4j2;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
//Cheat - mvn
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration
public class ECLocalOfficeConfiguration {

    @Value(value = "${office.home}")
    private String officeHome;

    @Value(value = "${office.port}")
    private int port = 8088;

    private LocalOfficeManager configuration;

    public LocalOfficeManager initOfficeManager() {
        log.info("ECLocalOfficeConfiguration");
        configuration = LocalOfficeManager.builder().portNumbers(port)
                .officeHome(officeHome)
                .install().build();
        return configuration;
    }

    @ConditionalOnMissingBean(name = "localOfficeManager")
    @Bean(initMethod = "start", destroyMethod = "stop")
    public LocalOfficeManager localOfficeManager() throws OfficeException {
        return initOfficeManager();
    }

    @Bean(name = "localDocumentConverter")
    @ConditionalOnBean(name = "localOfficeManager")
    public DocumentConverter localDocumentConverter(final LocalOfficeManager localOfficeManager) {
        return LocalConverter.make(localOfficeManager);
    }
}
