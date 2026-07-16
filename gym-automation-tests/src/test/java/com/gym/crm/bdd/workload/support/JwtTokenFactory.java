package com.gym.crm.bdd.workload.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gym.crm.bdd.support.CucumberObjectMapper;
import lombok.NoArgsConstructor;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import static com.gym.crm.bdd.workload.support.WorkloadComponentStack.JWT_SECRET_BASE64;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class JwtTokenFactory {

    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final String JWT_SEGMENT_SEPARATOR = ".";
    private static final Duration ACCESS_TOKEN_LIFETIME = Duration.ofMinutes(5);
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final String ENCODED_HEADER = createEncodedHeader();
    private static final SecretKeySpec SIGNING_KEY = createSigningKey();

    public static String create(String username) throws Exception {
        Instant now = Instant.now();
        Map<String, Object> payloadClaims = createClaims(username, now);
        String encodedPayload = encodeJson(payloadClaims);

        String unsignedToken = ENCODED_HEADER + JWT_SEGMENT_SEPARATOR + encodedPayload;
        String signature = sign(unsignedToken);

        return unsignedToken + JWT_SEGMENT_SEPARATOR + signature;
    }

    private static Map<String, Object> createClaims(String username, Instant issuedAt) {
        return Map.of("sub", username,
                "jti", randomUUID().toString(),
                "iat", issuedAt.getEpochSecond(),
                "exp", issuedAt.plus(ACCESS_TOKEN_LIFETIME).getEpochSecond());
    }

    private static String createEncodedHeader() {
        try {
            Map<String, String> headerClaims = Map.of("alg", "HS256",
                    "typ", "JWT");
            return encodeJson(headerClaims);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to encode JWT header", exception);
        }
    }

    private static SecretKeySpec createSigningKey() {
        byte[] signingKey = Base64.getDecoder().decode(JWT_SECRET_BASE64);
        return new SecretKeySpec(signingKey, HMAC_SHA_256);
    }

    private static String encodeJson(Map<String, ?> value) throws JsonProcessingException {
        byte[] json = CucumberObjectMapper.instance().writeValueAsBytes(value);
        return BASE64_URL_ENCODER.encodeToString(json);
    }

    private static String sign(String unsignedToken) throws Exception {
        Mac mac = createSigningMac();

        byte[] unsignedTokenBytes = unsignedToken.getBytes(UTF_8);
        byte[] signature = mac.doFinal(unsignedTokenBytes);

        return BASE64_URL_ENCODER.encodeToString(signature);
    }

    private static Mac createSigningMac() throws GeneralSecurityException {
        Mac mac = Mac.getInstance(HMAC_SHA_256);
        mac.init(SIGNING_KEY);

        return mac;
    }

}
