package com.gym.crm.core;

import com.gym.crm.logging.EnableTransactionLogging;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableTransactionLogging
public class GymCoreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymCoreServiceApplication.class, args);
    }

}
