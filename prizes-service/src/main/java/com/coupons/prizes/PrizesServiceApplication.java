package com.coupons.prizes;

import com.coupons.prizes.infra.security.IngressAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(IngressAuthProperties.class)
public class PrizesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrizesServiceApplication.class, args);
    }
}
