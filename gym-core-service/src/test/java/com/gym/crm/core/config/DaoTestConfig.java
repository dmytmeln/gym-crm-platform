package com.gym.crm.core.config;

import com.gym.crm.core.helper.TestDbClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;

import javax.sql.DataSource;

@TestConfiguration
public class DaoTestConfig {

    @Bean
    public JdbcClient jdbcClient(DataSource dataSource) {
        return JdbcClient.create(dataSource);
    }

    @Bean
    public TestDbClient testDbClient(JdbcClient jdbcClient) {
        return new TestDbClient(jdbcClient);
    }

}
