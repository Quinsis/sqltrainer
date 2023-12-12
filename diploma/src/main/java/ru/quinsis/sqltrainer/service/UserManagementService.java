package ru.quinsis.sqltrainer.service;

import ru.quinsis.sqltrainer.model.postgres.User;

import java.util.Optional;

public interface UserManagementService {
    Optional<User> activate(String code);
    Optional<User> reset(String code, String newPassword);
}
