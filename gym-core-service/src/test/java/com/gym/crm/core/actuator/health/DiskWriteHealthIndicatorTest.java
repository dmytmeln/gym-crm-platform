package com.gym.crm.core.actuator.health;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DiskWriteHealthIndicatorTest {

    @Test
    void shouldReturnUpWhenDirectoryIsWritable(@TempDir @NonNull Path tempDir) {
        DiskWriteHealthIndicator healthIndicator = new DiskWriteHealthIndicator(tempDir.toString());

        Health result = healthIndicator.health();

        assertThat(result.getStatus()).isEqualTo(Status.UP);
        assertThat(result.getDetails()).containsEntry("writable", true);
        assertThat(result.getDetails()).containsKey("directory");
    }

    @Test
    void shouldReturnDownWhenDirectoryCannotBeCreated(@TempDir @NonNull Path tempDir) throws IOException {
        String nonWritablePath = createNonWritablePath(tempDir);
        DiskWriteHealthIndicator healthIndicator = new DiskWriteHealthIndicator(nonWritablePath);

        Health result = healthIndicator.health();

        assertThat(result.getStatus()).isEqualTo(Status.DOWN);
        assertThat(result.getDetails()).containsEntry("writable", false);
        assertThat(result.getDetails()).containsKey("errorMessage");
    }

    private @NonNull String createNonWritablePath(@NonNull Path tempDir) throws IOException {
        Path regularFile = tempDir.resolve("regular-file");
        Files.writeString(regularFile, "dummy content");

        return createSubPathForFile(regularFile);
    }

    private @NonNull String createSubPathForFile(@NonNull Path regularFile) {
        return regularFile.resolve("sub-dir").toString();
    }

}
