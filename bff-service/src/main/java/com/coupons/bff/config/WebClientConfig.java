package com.coupons.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient authWebClient(WebClient.Builder builder, @Value("${coupons.services.auth-url}") String authBaseUrl) {
        return builder.baseUrl(authBaseUrl).build();
    }

    @Bean
    public WebClient campaignsWebClient(
            WebClient.Builder builder, @Value("${coupons.services.campaigns-url}") String campaignsBaseUrl) {
        return builder.baseUrl(campaignsBaseUrl).build();
    }

    @Bean
    public WebClient prizesWebClient(
            WebClient.Builder builder, @Value("${coupons.services.prizes-url}") String prizesBaseUrl) {
        return builder.baseUrl(prizesBaseUrl).build();
    }

    @Bean
    public WebClient profileWebClient(
            WebClient.Builder builder, @Value("${coupons.services.profile-url}") String profileBaseUrl) {
        return builder.baseUrl(profileBaseUrl).build();
    }

    @Bean
    public WebClient ledgerWebClient(
            WebClient.Builder builder, @Value("${coupons.services.ledger-url}") String ledgerBaseUrl) {
        return builder.baseUrl(ledgerBaseUrl).build();
    }

    @Bean
    public WebClient dailyChestWebClient(
            WebClient.Builder builder, @Value("${coupons.services.daily-chest-url}") String dailyChestBaseUrl) {
        return builder.baseUrl(dailyChestBaseUrl).build();
    }
}
