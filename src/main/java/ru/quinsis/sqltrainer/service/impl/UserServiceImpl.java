package ru.quinsis.sqltrainer.service.impl;

import ru.quinsis.sqltrainer.model.postgres.User;
import ru.quinsis.sqltrainer.repository.UserRepository;
import ru.quinsis.sqltrainer.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public Optional<User> getUserByResetPasswordCode(String code) {
        return userRepository.findByResetPasswordCode(code);
    }

    @Override
    public Optional<User> getUserByName(String name) {
        return userRepository.findByName(name);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> getUserByActivationCode(String activationCode) {
        return userRepository.findByActivationCode(activationCode);
    }
}
