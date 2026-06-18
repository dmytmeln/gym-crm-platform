package com.gym.crm.core.config;

import com.gym.crm.core.client.WorkloadClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfig {

    @Bean
    public WorkloadClient workloadClient(RestClient.Builder restClientBuilder, @Value("${app.services.workload.url}") String workloadUrl) {
        RestClient restClient = restClientBuilder
                .baseUrl(workloadUrl)
                .build();

        return createClient(restClient, WorkloadClient.class);
    }

    private <T> T createClient(RestClient restClient, Class<T> clientClass) {
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();

        return factory.createClient(clientClass);
    }

}
