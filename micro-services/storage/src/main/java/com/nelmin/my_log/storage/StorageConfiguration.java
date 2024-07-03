package com.nelmin.my_log.storage;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.nelmin.my_log", considerNestedRepositories = true)
public class StorageConfiguration {

    // TODO configure switch between providers
//    @Bean
//    public IStorageService storageService() {
//        return new JDBCStorageService();
//    }
}
