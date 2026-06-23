package com.gym.crm.core.config;

import com.gym.crm.client.TransactionIdPropagationInterceptor;
import com.gym.crm.core.client.WorkloadClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import com.gym.crm.core.security.TokenPropagationInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.http.HttpClient;
import java.time.Duration;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Configuration
public class ClientConfig {

    @Bean("baseRestClientBuilder")
    @Scope(SCOPE_PROTOTYPE)
    RestClient.Builder baseRestClientBuilder(RestClientBuilderConfigurer restClientBuilderConfigurer) {
        return restClientBuilderConfigurer.configure(RestClient.builder());
    }

    @LoadBalanced
    @Bean("loadBalancedRestClientBuilder")
    public RestClient.Builder loadBalancedRestClientBuilder(@Qualifier("baseRestClientBuilder") RestClient.Builder restClientBuilder) {
        return restClientBuilder;
    }

    @Bean
    public WorkloadClient workloadClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
                                         @Value("${app.services.workload.url}") String workloadUrl,
                                         TokenPropagationInterceptor tokenPropagationInterceptor,
                                         @Value("${app.services.workload.timeout-seconds:3}") int timeoutSeconds,
                                         TransactionIdPropagationInterceptor transactionIdPropagationInterceptor) {
        JdkClientHttpRequestFactory requestFactory = buildRequestFactory(timeoutSeconds);
        RestClient restClient = restClientBuilder
                .baseUrl(workloadUrl)
                .requestFactory(requestFactory)
                .requestInterceptor(tokenPropagationInterceptor)
                .requestInterceptor(transactionIdPropagationInterceptor)
                .build();

        return createClient(restClient, WorkloadClient.class);
    }

    @Bean
    public TransactionIdPropagationInterceptor transactionIdPropagationInterceptor() {
        return new TransactionIdPropagationInterceptor();
    }

    private JdkClientHttpRequestFactory buildRequestFactory(int timeoutSeconds) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        return requestFactory;
    }

    private <T> T createClient(RestClient restClient, Class<T> clientClass) {
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();

        return factory.createClient(clientClass);
    }

}
