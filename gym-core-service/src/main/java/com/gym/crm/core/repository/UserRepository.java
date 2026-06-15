package com.gym.crm.core.repository;

import com.gym.crm.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Query("SELECT u.username FROM User u WHERE u.username LIKE concat(:username, '%')")
    List<String> findUsernamesStartingWith(String username);

    @Query("SELECT u.isActive FROM User u WHERE u.username = :username")
    Optional<Boolean> isActive(String username);

}
