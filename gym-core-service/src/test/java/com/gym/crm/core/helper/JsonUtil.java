package com.gym.crm.core.helper;

import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class JsonUtil {

    public static String readJson(String resourcePath) {
        try (InputStream is = openResourceInputStream(resourcePath)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read JSON resource: " + resourcePath, e);
        }
    }

    private static @NonNull InputStream openResourceInputStream(String resourcePath) throws IOException {
        return new ClassPathResource(resourcePath).getInputStream();
    }

}
