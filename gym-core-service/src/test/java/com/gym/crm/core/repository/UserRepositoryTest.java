package com.gym.crm.core.repository;

import com.gym.crm.core.entity.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends AbstractRepositoryTest<UserRepository> {

    private static final long EXISTING_ID = 1L;
    private static final String EXISTING_USERNAME = "liam.miller";
    private static final String NON_EXISTING_USERNAME = "non.existent";

    @Test
    void shouldFindByUsernameWhenExists() {
        User expected = testDbClient.findUser(EXISTING_ID);

        Optional<User> actual = repository.findByUsername(EXISTING_USERNAME);

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyWhenNotFoundByUsername() {
        Optional<User> actual = repository.findByUsername(NON_EXISTING_USERNAME);

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldFindUsernamesStartingWith() {
        String prefix = "s";

        List<String> actual = repository.findUsernamesStartingWith(prefix);

        assertThat(actual)
                .hasSize(2)
                .containsExactlyInAnyOrder("sophia.wilson", "sarah.adams");
    }

    @Test
    void shouldReturnEmptyListWhenNoUsernamesStartingWith() {
        String prefix = "xyz";

        List<String> actual = repository.findUsernamesStartingWith(prefix);

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldReturnIsActiveTrueWhenUserIsActive() {
        Optional<Boolean> actual = repository.isActive("liam.miller");

        assertThat(actual).hasValue(true);
    }

    @Test
    void shouldReturnIsActiveFalseWhenUserIsNotActive() {
        Optional<Boolean> actual = repository.isActive("bob.wilson");

        assertThat(actual).hasValue(false);
    }

    @Test
    void shouldReturnEmptyIsActiveWhenUserDoesNotExist() {
        Optional<Boolean> actual = repository.isActive(NON_EXISTING_USERNAME);

        assertThat(actual).isEmpty();
    }

}
