package com.fashionpin.virtualtryonservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = {"com.fashionpin.virtualtryonservice", "com.fashionpin.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableKafka
public class VirtualTryonServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VirtualTryonServiceApplication.class, args);
    }
}

