package com.gym.crm.workload;

import com.gym.crm.workload.config.MongoContainerTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@ActiveProfiles("test")
class WorkloadServiceApplicationTest {

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        MongoContainerTestConfig.setMongoContainerProperties(registry);
    }

    @Test
    void contextLoads() {
    }

}
