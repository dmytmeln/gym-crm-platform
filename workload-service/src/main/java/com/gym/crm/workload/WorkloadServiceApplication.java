package com.gym.crm.workload;

import com.gym.crm.logging.EnableTransactionLogging;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableTransactionLogging
public class WorkloadServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkloadServiceApplication.class, args);
    }

}
