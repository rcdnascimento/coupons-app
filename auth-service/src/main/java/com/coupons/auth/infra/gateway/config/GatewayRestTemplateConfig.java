package com.coupons.auth.infra.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GatewayRestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder builder, @Value("${coupons.ingress.internal-api-key:}") String internalApiKey) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(10_000);
        RestTemplateBuilder b = builder.requestFactory(() -> factory);
        if (internalApiKey != null && !internalApiKey.isBlank()) {
            b = b.additionalInterceptors(
                    (request, body, execution) -> {
                        request.getHeaders().set("X-Internal-Api-Key", internalApiKey);
                        return execution.execute(request, body);
                    });
        }
        return b.build();
    }
}
