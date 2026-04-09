package com.coupons.bff.config;

import com.coupons.bff.security.BffJwtService;
import com.coupons.bff.security.JsonAccessDeniedHandler;
import com.coupons.bff.security.JsonAuthenticationEntryPoint;
import com.coupons.bff.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            JsonAuthenticationEntryPoint authenticationEntryPoint, JsonAccessDeniedHandler accessDeniedHandler) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(BffJwtService jwtService, ObjectMapper om) {
        return new JwtAuthenticationFilter(jwtService, om);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter)
            throws Exception {

        http.csrf()
                .disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
                .and()
                .cors()
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**")
                .permitAll()
                .antMatchers("/error")
                .permitAll()
                .antMatchers("/actuator/health", "/actuator/info")
                .permitAll()
                .antMatchers("/api/auth/**")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/api/uploads/images/**")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/api/campaigns/*/coupons")
                .hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/campaigns/*/coupons")
                .hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/campaigns/*/coupons/*")
                .hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/campaigns/*/subscriptions")
                .authenticated()
                .antMatchers(HttpMethod.GET, "/api/campaigns/*/subscriptions/me")
                .authenticated()
                .antMatchers(HttpMethod.POST, "/api/campaigns")
                .hasRole("ADMIN")
                .antMatchers(HttpMethod.PATCH, "/api/campaigns/*")
                .hasRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/api/campaigns/*/summary")
                .authenticated()
                .antMatchers(HttpMethod.GET, "/api/campaigns/*/winners")
                .authenticated()
                .antMatchers(HttpMethod.GET, "/api/campaigns")
                .authenticated()
                .antMatchers(HttpMethod.GET, "/api/campaigns/*")
                .authenticated()
                .antMatchers("/api/coupons/**")
                .hasRole("ADMIN")
                .antMatchers("/api/companies/**")
                .hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/uploads/images")
                .hasRole("ADMIN")
                .antMatchers("/api/admin/**")
                .hasRole("ADMIN")
                .antMatchers("/api/me/**")
                .authenticated()
                .antMatchers("/api/daily-chest/**")
                .authenticated()
                .antMatchers(HttpMethod.GET, "/api/prizes/me")
                .authenticated()
                .anyRequest()
                .denyAll()
                .and()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
