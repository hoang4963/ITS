package com.its.econtract.configuration;

import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Log4j2
@Configuration
@PropertySource({"classpath:quartz-db.properties"})
public class SchedulerConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private QuartzProperties quartzProperties;

    @Autowired
    private Environment env;

    private DataSource dataSource_;

    public DataSource quartzDataSource() {
        if (dataSource_ == null) {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(env.getProperty("jdbc.driverClassName"));
            dataSource.setUrl(env.getProperty("jdbc.url"));
            dataSource.setUsername(env.getProperty("jdbc.user"));
            dataSource.setPassword(env.getProperty("jdbc.password"));
            dataSource_ = dataSource;
            return dataSource;
        }
        return dataSource_;
    }

    @Bean
    public SpringLiquibase liquibase() {
        log.info("[BEGIN] liquibase |=====================>>");
        return new EContractLiquibase(quartzDataSource());
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {

        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);

        Properties properties = new Properties();
        properties.putAll(quartzProperties.getProperties());

        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(true);
        factory.setDataSource(quartzDataSource());
        factory.setQuartzProperties(properties);
        factory.setJobFactory(jobFactory);
        return factory;
    }
}
