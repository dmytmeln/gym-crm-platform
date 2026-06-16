package com.gym.crm.core.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JsonBodySanitizerTest {

    @Test
    void shouldSupportJsonContentTypes() {
        String contentType = "application/json";

        boolean result = JsonBodySanitizer.supports(contentType);

        assertThat(result).isTrue();
    }

    @Test
    void shouldSupportCustomJsonContentTypes() {
        String contentType = "application/merge-patch+json";

        boolean result = JsonBodySanitizer.supports(contentType);

        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"text/html", "*/*", "invalid-type/"})
    void shouldNotSupportInvalidContentTypes(String contentType) {
        boolean result = JsonBodySanitizer.supports(contentType);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnEmptyStringWhenBodyIsNull() {
        String result = JsonBodySanitizer.sanitize(null);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyStringWhenBodyIsEmpty() {
        String body = "";

        String result = JsonBodySanitizer.sanitize(body);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyStringWhenBodyIsBlank() {
        String body = "   ";

        String result = JsonBodySanitizer.sanitize(body);

        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideSanitizeTestData")
    void shouldSanitizeJsonCorrectly(String inputBody, String expectedResult) {
        String result = JsonBodySanitizer.sanitize(inputBody);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void shouldTruncateLongTextField() {
        String longText = "a".repeat(210);
        String body = "{\"name\": \"" + longText + "\"}";
        String expectedValue = "a".repeat(200) + "...[truncated]";

        String result = JsonBodySanitizer.sanitize(body);

        assertThat(result).isEqualTo("{\"name\":\"" + expectedValue + "\"}");
    }

    @Test
    void shouldTruncateBodyWhenSerializedBodyExceedsLimit() {
        String longText = "a".repeat(5010);
        String expected = longText.substring(0, 5000) + "...[body truncated]";

        String result = JsonBodySanitizer.sanitize(longText);

        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> provideSanitizeTestData() {
        return Stream.of(
                Arguments.of("Hello, World!",
                        "Hello, World!"),
                Arguments.of("{\"password\": \"secret123\", \"oldpassword\": \"old123\", \"newpassword\": \"new123\"}",
                        "{\"password\":\"***\",\"oldpassword\":\"***\",\"newpassword\":\"***\"}"),
                Arguments.of("{\"Password\": \"secret123\", \"OLDpassword\": \"old123\", \"NewPassword\": \"new123\"}",
                        "{\"Password\":\"***\",\"OLDpassword\":\"***\",\"NewPassword\":\"***\"}"),
                Arguments.of("{\"password\": null}",
                        "{\"password\":null}"),
                Arguments.of("{\"address\": \"123\"}",
                        "{\"address\":\"***\"}"),
                Arguments.of("{\"address\": \"12345678\"}",
                        "{\"address\":\"12***\"}"),
                Arguments.of("{\"address\": 12345}",
                        "{\"address\":12345}"),
                Arguments.of("{\"address\": null}",
                        "{\"address\":null}"),
                Arguments.of("{\"user\": {\"password\": \"secret\"}, \"items\": [{\"address\": \"12345678\"}]}",
                        "{\"user\":{\"password\":\"***\"},\"items\":[{\"address\":\"12***\"}]}")
        );
    }

}
