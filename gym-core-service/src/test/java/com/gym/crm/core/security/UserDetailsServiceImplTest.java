package com.gym.crm.core.security;

import com.gym.crm.core.entity.User;
import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.repository.UserRepository;
import com.gym.crm.core.security.UserDetailsServiceImpl.SimpleUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    private static final String USERNAME = "liam.miller";
    private static final String PASSWORD = "hashedPassword";

    @Mock
    private UserRepository userRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private UserDetailsServiceImpl service;

    @Test
    void shouldLoadUserByUsernameSuccessfullyAsTrainee() {
        User user = buildUser(true);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(traineeRepository.existsByUserUsername(USERNAME)).thenReturn(true);

        UserDetails result = service.loadUserByUsername(USERNAME);

        assertNotNull(result);
        assertEquals(USERNAME, result.getUsername());
        assertEquals(PASSWORD, result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TRAINEE")));
    }

    @Test
    void shouldLoadUserByUsernameSuccessfullyAsTrainer() {
        User user = buildUser(true);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(traineeRepository.existsByUserUsername(USERNAME)).thenReturn(false);
        when(trainerRepository.existsByUserUsername(USERNAME)).thenReturn(true);

        UserDetails result = service.loadUserByUsername(USERNAME);

        assertNotNull(result);
        assertEquals(USERNAME, result.getUsername());
        assertEquals(PASSWORD, result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TRAINER")));
    }

    @Test
    void shouldLoadUserByUsernameAsDisabledWhenUserIsDeactivated() {
        User user = buildUser(false);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(traineeRepository.existsByUserUsername(USERNAME)).thenReturn(true);

        UserDetails result = service.loadUserByUsername(USERNAME);

        assertNotNull(result);
        assertEquals(USERNAME, result.getUsername());
        assertEquals(PASSWORD, result.getPassword());
        assertFalse(result.isEnabled());
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(USERNAME));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenUserIsNeitherTraineeNorTrainer() {
        User user = buildUser(true);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(traineeRepository.existsByUserUsername(USERNAME)).thenReturn(false);
        when(trainerRepository.existsByUserUsername(USERNAME)).thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.loadUserByUsername(USERNAME));

        assertTrue(exception.getMessage().contains("neither in trainee nor trainer table"));
    }

    @Test
    void shouldloadSimpleUserByUsernameSuccessfullyAsTrainee() {
        when(userRepository.isActive(USERNAME)).thenReturn(Optional.of(true));
        when(traineeRepository.existsByUserUsername(USERNAME)).thenReturn(true);

        SimpleUserDetails result = service.loadSimpleUserByUsername(USERNAME);

        assertNotNull(result);
        assertEquals(USERNAME, result.getUsername());
        assertTrue(result.isEnabled());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TRAINEE")));
    }

    @Test
    void shouldloadSimpleUserByUsernameSuccessfullyAsTrainer() {
        when(userRepository.isActive(USERNAME)).thenReturn(Optional.of(true));
        when(traineeRepository.existsByUserUsername(USERNAME)).thenReturn(false);
        when(trainerRepository.existsByUserUsername(USERNAME)).thenReturn(true);

        SimpleUserDetails result = service.loadSimpleUserByUsername(USERNAME);

        assertNotNull(result);
        assertEquals(USERNAME, result.getUsername());
        assertTrue(result.isEnabled());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TRAINER")));
    }

    @Test
    void shouldloadSimpleUserByUsernameAsDisabledWhenUserIsDeactivated() {
        when(userRepository.isActive(USERNAME)).thenReturn(Optional.of(false));
        when(traineeRepository.existsByUserUsername(USERNAME)).thenReturn(true);

        SimpleUserDetails result = service.loadSimpleUserByUsername(USERNAME);

        assertNotNull(result);
        assertEquals(USERNAME, result.getUsername());
        assertFalse(result.isEnabled());
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenSimpleUserDetailsDoesNotExist() {
        when(userRepository.isActive(USERNAME)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> service.loadSimpleUserByUsername(USERNAME));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenSimpleUserDetailsIsNeitherTraineeNorTrainer() {
        when(userRepository.isActive(USERNAME)).thenReturn(Optional.of(true));
        when(traineeRepository.existsByUserUsername(USERNAME)).thenReturn(false);
        when(trainerRepository.existsByUserUsername(USERNAME)).thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.loadSimpleUserByUsername(USERNAME));

        assertTrue(exception.getMessage().contains("neither in trainee nor trainer table"));
    }

    private User buildUser(boolean isActive) {
        return User.builder()
                .firstName("Liam")
                .lastName("Miller")
                .username(USERNAME)
                .password(PASSWORD)
                .isActive(isActive)
                .build();
    }

}
