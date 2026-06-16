package com.gym.crm.core.actuator.health;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class HikariPoolSaturationHealthIndicator implements HealthIndicator {

    private static final double SATURATION_THRESHOLD_PERCENT = 0.85;

    private final DataSource dataSource;

    @Override
    public Health health() {
        if (!(dataSource instanceof HikariDataSource hikari)) {
            return Health.unknown()
                    .withDetail("error", "DataSource is not an instance of HikariDataSource")
                    .build();
        }

        if (hikari.getHikariPoolMXBean() == null) {
            return Health.unknown()
                    .withDetail("error", "Hikari pool is not yet initialized")
                    .build();
        }

        int active = hikari.getHikariPoolMXBean().getActiveConnections();
        int total = hikari.getHikariPoolMXBean().getTotalConnections();
        int max = hikari.getMaximumPoolSize();

        double utilization = max > 0
                ? (double) active / max
                : 0.0;

        Health.Builder builder = Health.up()
                .withDetail("activeConnections", active)
                .withDetail("totalConnections", total)
                .withDetail("maxPoolSize", max)
                .withDetail("utilization", String.format(Locale.US, "%.2f%%", utilization * 100));

        if (utilization < SATURATION_THRESHOLD_PERCENT) {
            return builder.build();
        }

        return builder.outOfService()
                .withDetail("warning", "Hikari connection pool saturation is high")
                .build();
    }

}
