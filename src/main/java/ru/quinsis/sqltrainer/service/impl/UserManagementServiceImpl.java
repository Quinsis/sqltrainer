package ru.quinsis.sqltrainer.service.impl;

import ru.quinsis.sqltrainer.model.postgres.User;
import ru.quinsis.sqltrainer.repository.UserRepository;
import ru.quinsis.sqltrainer.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> activate(String code) {
        return userRepository.findByActivationCode(code).map(user -> {
            user.setActivationCode(null);
            user.setActive(true);
            return userRepository.save(user);
        });
    }

    @Override
    public Optional<User> reset(String code, String newPassword) {
        return userRepository.findByResetPasswordCode(code).map(user -> {
            user.setResetPasswordCode(null);
            user.setPassword(passwordEncoder.encode(newPassword));
            return userRepository.save(user);
        });
    }
}
