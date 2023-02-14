package com.its.econtract.configuration;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.log4j.Log4j2;

import javax.sql.DataSource;


@Log4j2
public class EContractLiquibase extends SpringLiquibase {

    public EContractLiquibase(DataSource ds) {
        dataSource = ds;
        super.setChangeLog("classpath:database/master.xml");
        super.setDataSource(ds);
    }

    @Override
    public void afterPropertiesSet() throws LiquibaseException {
        try {
            super.afterPropertiesSet();
        } catch (LiquibaseException e) {
            log.error("Cause", e);
        }
    }
}
