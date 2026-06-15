package com.gym.crm.core.security;

import com.gym.crm.core.entity.User;
import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.gym.crm.core.security.Role.ROLE_TRAINEE;
import static com.gym.crm.core.security.Role.ROLE_TRAINER;
import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Role role = getUserRole(username);

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.getIsActive())
                .authorities(List.of(new SimpleGrantedAuthority(role.name())))
                .build();
    }

    public SimpleUserDetails loadSimpleUserByUsername(String username) throws UsernameNotFoundException {
        Boolean active = userRepository.isActive(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Role role = getUserRole(username);

        return new SimpleUserDetails(username, active, List.of(new SimpleGrantedAuthority(role.name())));
    }

    private Role getUserRole(String username) {
        if (traineeRepository.existsByUserUsername(username)) {
            return ROLE_TRAINEE;
        }

        if (trainerRepository.existsByUserUsername(username)) {
            return ROLE_TRAINER;
        }

        throw new IllegalStateException(format("User with username %s exists in user table but neither in trainee nor trainer table", username));
    }

    @RequiredArgsConstructor
    public static class SimpleUserDetails implements UserDetails {

        private final String username;
        private final boolean activated;
        private final Collection<GrantedAuthority> authorities;

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public boolean isEnabled() {
            return activated;
        }

        @Override
        public String getPassword() {
            return null;
        }

    }

}

