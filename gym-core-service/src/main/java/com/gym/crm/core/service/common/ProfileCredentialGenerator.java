package com.gym.crm.core.service.common;

import com.gym.crm.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileCredentialGenerator {

    private static final String PASSWORD_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
    private static final int PASSWORD_LENGTH = 10;
    private static final String USERNAME_SEPARATOR = ".";

    private final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository userRepository;

    public String generateUsername(String firstName, String lastName) {
        Objects.requireNonNull(firstName, "First Name cannot be null");
        Objects.requireNonNull(lastName, "Last Name cannot be null");

        String baseUsername = buildBaseUsername(firstName, lastName);
        log.debug("Generating username for base: {}", baseUsername);

        List<String> takenUsernames = findUsernamesWithSameBase(baseUsername);
        if (takenUsernames.isEmpty()) {
            return baseUsername;
        }

        log.debug("Found {} existing usernames for base: {}", takenUsernames.size(), baseUsername);
        int serialNumber = nextSerialNumber(baseUsername, takenUsernames);
        String finalUsername = baseUsername + serialNumber;
        log.debug("Generated username with serial number: {}", finalUsername);

        return finalUsername;
    }

    public String generatePassword() {
        char[] password = new char[PASSWORD_LENGTH];
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int alphabetIndex = secureRandom.nextInt(PASSWORD_ALPHABET.length());
            password[i] = PASSWORD_ALPHABET.charAt(alphabetIndex);
        }

        return new String(password);
    }

    private String buildBaseUsername(String firstName, String lastName) {
        return firstName.toLowerCase(Locale.ROOT) + USERNAME_SEPARATOR + lastName.toLowerCase(Locale.ROOT);
    }

    private List<String> findUsernamesWithSameBase(String baseUsername) {
        return userRepository.findUsernamesStartingWith(baseUsername).stream()
                .filter(username -> matchesBaseUsername(username, baseUsername))
                .toList();
    }

    private boolean matchesBaseUsername(String username, String baseUsername) {
        String lowerUsername = username.toLowerCase();
        String lowerBaseUsername = baseUsername.toLowerCase();

        if (!lowerUsername.startsWith(lowerBaseUsername)) {
            return false;
        }

        if (lowerUsername.equals(lowerBaseUsername)) {
            return true;
        }

        String suffix = lowerUsername.substring(lowerBaseUsername.length());
        return suffix.matches("\\d+");
    }

    private int nextSerialNumber(String baseUsername, List<String> existingUsernames) {
        int maxSerialNumber = findMaxSerialNumber(baseUsername, existingUsernames);
        return maxSerialNumber + 1;
    }

    private int findMaxSerialNumber(String baseUsername, List<String> existingUsernames) {
        return existingUsernames.stream()
                .mapToInt(username -> extractSerialNumber(username, baseUsername))
                .max()
                .orElse(0);
    }

    private int extractSerialNumber(String username, String baseUsername) {
        String suffix = username.substring(baseUsername.length());

        return suffix.isEmpty()
                ? 0
                : Integer.parseInt(suffix);
    }

}
