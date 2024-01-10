package ru.quinsis.sqltrainer.service;

import ru.quinsis.sqltrainer.model.postgres.User;

import java.util.Optional;

public interface UserMailService {
    Optional<User> forgot(String email);
    String registration(User user);
}
