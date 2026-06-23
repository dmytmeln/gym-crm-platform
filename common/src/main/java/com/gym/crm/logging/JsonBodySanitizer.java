package com.gym.crm.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.NoArgsConstructor;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@NoArgsConstructor(access = PRIVATE)
public final class JsonBodySanitizer {

    private static final int MAX_BODY_CHARS = 5000;
    private static final int MAX_FIELD_CHARS = 200;
    private static final Set<String> MASKED_FIELDS = Set.of("password", "oldpassword", "newpassword");
    private static final Set<String> PARTIAL_FIELDS = Set.of("address");
    private static final List<SanitizingRule> SANITIZING_RULES = List.of(new FullMaskingRule(),
            new PartialMaskingRule(),
            new TextTruncationRule());

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String sanitize(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }

        try {
            JsonNode node = mapper.readTree(body);
            JsonNode masked = maskNode(node);
            String maskedJson = mapper.writeValueAsString(masked);

            return truncateBody(maskedJson);
        } catch (JsonProcessingException e) {
            return truncateBody(body);
        }
    }

    public static boolean supports(String contentType) {
        if (contentType == null) {
            return false;
        }

        try {
            MediaType mediaType = MediaType.valueOf(contentType);

            return APPLICATION_JSON.equalsTypeAndSubtype(mediaType) || contentType.contains("json");
        } catch (InvalidMediaTypeException ignored) {
            return false;
        }
    }

    private static JsonNode maskNode(JsonNode node) {
        if (!node.isObject() && !node.isArray()) {
            return node;
        }

        if (node.isArray()) {
            ArrayNode arr = mapper.createArrayNode();
            node.forEach(element -> arr.add(maskNode(element)));
            return arr;
        }

        ObjectNode obj = (ObjectNode) node;
        maskObjectNodes(obj);

        return obj;
    }

    private static void maskObjectNodes(ObjectNode obj) {
        List<String> fieldNames = new ArrayList<>();
        obj.fieldNames().forEachRemaining(fieldNames::add);

        for (String key : fieldNames) {
            JsonNode originalValue = obj.get(key);
            JsonNode sanitizedValue = sanitizeValue(key, originalValue);
            obj.set(key, sanitizedValue);
        }
    }

    private static JsonNode sanitizeValue(String key, JsonNode value) {
        if (value.isContainerNode()) {
            return maskNode(value);
        }

        return SANITIZING_RULES.stream()
                .filter(rule -> rule.matches(key, value))
                .findFirst()
                .map(rule -> rule.apply(value))
                .orElse(value);
    }

    private static String truncateBody(String body) {
        if (body.length() <= MAX_BODY_CHARS) {
            return body;
        }

        return body.substring(0, MAX_BODY_CHARS) + "...[body truncated]";
    }

    private interface SanitizingRule {
        boolean matches(String key, JsonNode value);

        JsonNode apply(JsonNode value);
    }

    private static class FullMaskingRule implements SanitizingRule {
        @Override
        public boolean matches(String key, JsonNode value) {
            return MASKED_FIELDS.contains(key.toLowerCase(Locale.ROOT)) && !value.isNull();
        }

        @Override
        public JsonNode apply(JsonNode value) {
            return TextNode.valueOf("***");
        }
    }

    private static class PartialMaskingRule implements SanitizingRule {
        @Override
        public boolean matches(String key, JsonNode value) {
            return PARTIAL_FIELDS.contains(key.toLowerCase(Locale.ROOT)) && value.isTextual();
        }

        @Override
        public JsonNode apply(JsonNode value) {
            return TextNode.valueOf(maskValuePartially(value.asText()));
        }

        private String maskValuePartially(String value) {
            if (value.length() <= 4) {
                return "***";
            }

            int visible = Math.min(4, value.length() / 4);
            return value.substring(0, visible) + "***";
        }
    }

    private static class TextTruncationRule implements SanitizingRule {
        @Override
        public boolean matches(String key, JsonNode value) {
            return value.isTextual();
        }

        @Override
        public JsonNode apply(JsonNode value) {
            return TextNode.valueOf(truncateValue(value.asText()));
        }

        private String truncateValue(String value) {
            if (value.length() <= MAX_FIELD_CHARS) {
                return value;
            }

            return value.substring(0, MAX_FIELD_CHARS) + "...[truncated]";
        }
    }

}
