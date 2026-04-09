package com.coupons.campaigns;

import com.coupons.campaigns.infra.security.IngressAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableKafka
@EnableConfigurationProperties(IngressAuthProperties.class)
public class CampaignsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampaignsServiceApplication.class, args);
    }
}
