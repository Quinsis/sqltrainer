package ru.quinsis.sqltrainer.service;

import ru.quinsis.sqltrainer.model.postgres.User;

import java.util.Optional;

public interface UserService {
    Optional<User> getUserByResetPasswordCode(String code);
    Optional<User> getUserByName(String name);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByEmail(String email);
    Optional<User> getUserByActivationCode(String activationCode);
}
