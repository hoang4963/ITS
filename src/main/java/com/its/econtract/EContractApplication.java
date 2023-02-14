package com.its.econtract;

import com.its.econtract.entity.ECDocuments;
import com.its.econtract.repository.ECDocumentRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Log4j2
@SpringBootApplication
@EnableAsync
public class EContractApplication {

    public static void main(String[] args) {
        SpringApplication.run(EContractApplication.class, args);
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("EContract-");
        executor.initialize();
        return executor;
    }

    @Autowired
    private ECDocumentRepository documentRepository;

    @Bean
    CommandLineRunner start() {
        return args -> {
            ECDocuments dc = documentRepository.getById(30);
            System.out.println(dc.getId());
        };
    }
}
