package ru.quinsis.sqltrainer.repository;

import ru.quinsis.sqltrainer.model.postgres.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String email);
    Optional<User> findByActivationCode(String activationCode);
    Optional<User> findByResetPasswordCode(String resetPasswordCode);
}
