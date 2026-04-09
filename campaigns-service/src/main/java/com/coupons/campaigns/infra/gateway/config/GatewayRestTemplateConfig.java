package com.coupons.campaigns.infra.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GatewayRestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(@Value("${coupons.ingress.internal-api-key:}") String internalApiKey) {
        RestTemplate restTemplate = new RestTemplate();
        if (internalApiKey != null && !internalApiKey.isBlank()) {
            restTemplate
                    .getInterceptors()
                    .add(
                            (request, body, execution) -> {
                                request.getHeaders().set("X-Internal-Api-Key", internalApiKey);
                                return execution.execute(request, body);
                            });
        }
        return restTemplate;
    }
}
