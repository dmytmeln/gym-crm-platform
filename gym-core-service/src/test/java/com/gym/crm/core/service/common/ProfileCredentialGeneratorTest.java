package com.gym.crm.core.service.common;

import com.gym.crm.core.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileCredentialGeneratorTest {

    private static final String PASSWORD_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
    private static final int PASSWORD_LENGTH = 10;
    private static final String FIRST_NAME = "Liam";
    private static final String LAST_NAME = "Miller";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileCredentialGenerator generator;

    @Test
    void shouldGenerateUsernameWithoutSerialWhenNoExistingUsers() {
        when(userRepository.findUsernamesStartingWith("liam.miller")).thenReturn(Collections.emptyList());

        String actual = generator.generateUsername(FIRST_NAME, LAST_NAME);

        assertEquals("liam.miller", actual);
        verify(userRepository).findUsernamesStartingWith("liam.miller");
    }

    @Test
    void shouldAppendNextSerialWhenBaseUsernameIsTaken() {
        when(userRepository.findUsernamesStartingWith("liam.miller")).thenReturn(List.of("Liam.Miller"));

        String actual = generator.generateUsername(FIRST_NAME, LAST_NAME);

        assertEquals("liam.miller1", actual);
    }

    @Test
    void shouldUseHighestSerialPlusOneWhenMatchingUsernamesExist() {
        when(userRepository.findUsernamesStartingWith("liam.miller")).thenReturn(List.of("liam.miller", "Liam.Miller1", "liam.Miller2"));

        String actual = generator.generateUsername(FIRST_NAME, LAST_NAME);

        assertEquals("liam.miller3", actual);
    }

    @Test
    void shouldUseHighestSerialPlusOneWhenSerialGapsExist() {
        when(userRepository.findUsernamesStartingWith("liam.miller")).thenReturn(List.of("Liam.Miller", "liam.Miller2", "liam.Miller5"));

        String actual = generator.generateUsername(FIRST_NAME, LAST_NAME);

        assertEquals("liam.miller6", actual);
    }

    @Test
    void shouldHandleMultipleMatchingUsers() {
        when(userRepository.findUsernamesStartingWith("liam.miller")).thenReturn(List.of("liam.miller", "liam.miller1", "liam.miller3"));

        String actual = generator.generateUsername(FIRST_NAME, LAST_NAME);

        assertEquals("liam.miller4", actual);
        verify(userRepository).findUsernamesStartingWith("liam.miller");
    }

    @ParameterizedTest
    @CsvSource({
            "sophia.miller, liam.miller",
            "liam.wilson, liam.miller",
            "liam.miller, liam.miller1"
    })
    void shouldHandleUsernameMatching(String existingUsername, String expectedUsername) {
        when(userRepository.findUsernamesStartingWith("liam.miller")).thenReturn(List.of(existingUsername));

        String actual = generator.generateUsername(FIRST_NAME, LAST_NAME);

        assertEquals(expectedUsername, actual);
    }

    @Test
    void shouldIgnoreUsersWithDifferentFullName() {
        when(userRepository.findUsernamesStartingWith("liam.miller")).thenReturn(List.of("sophia.wilson"));

        String actual = generator.generateUsername(FIRST_NAME, LAST_NAME);

        assertEquals("liam.miller", actual);
    }

    @Test
    void shouldIgnoreUsernamesWithNonNumericSuffix() {
        when(userRepository.findUsernamesStartingWith("liam.miller")).thenReturn(List.of("liam.millerX", "liam.miller_1"));

        String actual = generator.generateUsername(FIRST_NAME, LAST_NAME);

        assertEquals("liam.miller", actual);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenFirstNameIsNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> generator.generateUsername(null, LAST_NAME));

        assertEquals("First Name cannot be null", exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenLastNameIsNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> generator.generateUsername(FIRST_NAME, null));

        assertEquals("Last Name cannot be null", exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenBothNamesAreNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> generator.generateUsername(null, null));

        assertEquals("First Name cannot be null", exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldGeneratePasswordWithCorrectLength() {
        String actual = generator.generatePassword();

        assertEquals(PASSWORD_LENGTH, actual.length());
    }

    @Test
    void shouldGeneratePasswordWithValidCharactersOnly() {
        String actual = generator.generatePassword();

        assertTrue(StringUtils.containsOnly(actual, PASSWORD_ALPHABET), "Password contains invalid characters");
    }

}
