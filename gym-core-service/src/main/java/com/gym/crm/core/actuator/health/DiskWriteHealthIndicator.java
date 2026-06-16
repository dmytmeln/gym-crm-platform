package com.gym.crm.core.actuator.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Component
public class DiskWriteHealthIndicator implements HealthIndicator {

    private final Path logDirPath;

    public DiskWriteHealthIndicator(@Value("${app.logging.path:logs}") String logDirPathStr) {
        this.logDirPath = Paths.get(logDirPathStr);
    }

    @Override
    public Health health() {
        Path tempFile = logDirPath.resolve("health-check-" + UUID.randomUUID() + ".tmp");

        try {
            if (!Files.exists(logDirPath)) {
                Files.createDirectories(logDirPath);
            }

            Files.writeString(tempFile, "write-test");
            Files.delete(tempFile);

            return Health.up()
                    .withDetail("directory", logDirPath.toAbsolutePath().toString())
                    .withDetail("writable", true)
                    .build();
        } catch (IOException ex) {
            log.error("Log directory is not writable", ex);
            return Health.down(ex)
                    .withDetail("directory", logDirPath.toAbsolutePath().toString())
                    .withDetail("writable", false)
                    .withDetail("errorMessage", ex.getMessage())
                    .build();
        }
    }

}
