package com.fashionpin.searchservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = {"com.fashionpin.searchservice", "com.fashionpin.search", "com.fashionpin.common"})
@EntityScan({"com.fashionpin.searchservice.entity", "com.fashionpin.search.domain.model"})
@EnableJpaRepositories({"com.fashionpin.searchservice.repository", "com.fashionpin.search.repository"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableKafka
public class SearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
