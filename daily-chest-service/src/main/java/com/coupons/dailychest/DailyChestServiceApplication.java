package com.coupons.dailychest;

import com.coupons.dailychest.infra.security.IngressAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(IngressAuthProperties.class)
public class DailyChestServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DailyChestServiceApplication.class, args);
    }
}
