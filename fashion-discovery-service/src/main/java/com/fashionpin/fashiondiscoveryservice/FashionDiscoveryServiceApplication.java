package com.fashionpin.fashiondiscoveryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = {"com.fashionpin.fashiondiscoveryservice", "com.fashionpin.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableKafka
public class FashionDiscoveryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FashionDiscoveryServiceApplication.class, args);
    }
}

