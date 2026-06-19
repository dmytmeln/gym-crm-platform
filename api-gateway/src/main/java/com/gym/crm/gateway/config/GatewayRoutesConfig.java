package com.gym.crm.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("workload_route", r -> r
                .path("/gym-crm/workload/**")
                .uri("lb://workload-service"))
            .route("gym_core_route", r -> r
                .path("/gym-crm/core/**")
                .uri("lb://gym-core-service"))
            .build();
    }

}
