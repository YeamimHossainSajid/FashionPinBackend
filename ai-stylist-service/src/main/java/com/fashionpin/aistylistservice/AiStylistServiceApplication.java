package com.fashionpin.aistylistservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = {"com.fashionpin.aistylistservice", "com.fashionpin.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableKafka
public class AiStylistServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiStylistServiceApplication.class, args);
    }
}

