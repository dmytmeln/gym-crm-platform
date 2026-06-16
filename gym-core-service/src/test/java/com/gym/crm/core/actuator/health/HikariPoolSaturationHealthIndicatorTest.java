package com.gym.crm.core.actuator.health;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HikariPoolSaturationHealthIndicatorTest {

    @Mock
    private HikariDataSource hikariDataSource;

    @InjectMocks
    private HikariPoolSaturationHealthIndicator healthIndicator;

    @Test
    void shouldReturnUpWhenPoolSaturationIsBelowThreshold() {
        HikariPoolMXBean poolMXBean = mock(HikariPoolMXBean.class);

        when(hikariDataSource.getHikariPoolMXBean()).thenReturn(poolMXBean);
        when(poolMXBean.getActiveConnections()).thenReturn(5);
        when(poolMXBean.getTotalConnections()).thenReturn(10);
        when(hikariDataSource.getMaximumPoolSize()).thenReturn(10);

        Health result = healthIndicator.health();

        assertThat(result.getStatus()).isEqualTo(Status.UP);
        assertThat(result.getDetails()).containsEntry("activeConnections", 5);
        assertThat(result.getDetails()).containsEntry("totalConnections", 10);
        assertThat(result.getDetails()).containsEntry("maxPoolSize", 10);
        assertThat(result.getDetails()).containsEntry("utilization", "50.00%");
    }

    @Test
    void shouldReturnOutOfServiceWhenPoolSaturationIsAtOrAboveThreshold() {
        HikariPoolMXBean poolMXBean = mock(HikariPoolMXBean.class);

        when(hikariDataSource.getHikariPoolMXBean()).thenReturn(poolMXBean);
        when(poolMXBean.getActiveConnections()).thenReturn(9);
        when(poolMXBean.getTotalConnections()).thenReturn(10);
        when(hikariDataSource.getMaximumPoolSize()).thenReturn(10);

        Health result = healthIndicator.health();

        assertThat(result.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
        assertThat(result.getDetails()).containsEntry("activeConnections", 9);
        assertThat(result.getDetails()).containsEntry("utilization", "90.00%");
        assertThat(result.getDetails()).containsEntry("warning", "Hikari connection pool saturation is high");
    }

    @Test
    void shouldReturnUnknownWhenHikariPoolNotInitialized() {
        when(hikariDataSource.getHikariPoolMXBean()).thenReturn(null);

        Health result = healthIndicator.health();

        assertThat(result.getStatus()).isEqualTo(Status.UNKNOWN);
        assertThat(result.getDetails()).containsEntry("error", "Hikari pool is not yet initialized");
    }

    @Test
    void shouldReturnUnknownWhenNotHikariDataSource() {
        DataSource standardDataSource = mock(DataSource.class);
        HikariPoolSaturationHealthIndicator standardIndicator = new HikariPoolSaturationHealthIndicator(standardDataSource);

        Health result = standardIndicator.health();

        assertThat(result.getStatus()).isEqualTo(Status.UNKNOWN);
        assertThat(result.getDetails()).containsEntry("error", "DataSource is not an instance of HikariDataSource");
    }

}
