package ru.quinsis.sqltrainer.service.impl;

import ru.quinsis.sqltrainer.model.mongodb.Task;
import ru.quinsis.sqltrainer.model.postgres.User;
import ru.quinsis.sqltrainer.repository.UserRepository;
import ru.quinsis.sqltrainer.service.UserMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserMailServiceImpl implements UserMailService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    @Value("${spring.email.registration-message}")
    private String registrationMessage;
    @Value("${spring.email.password-reset-message}")
    private String passwordResetMessage;
    @Value("${spring.email.task-complete-message}")
    private String taskCompleteMessage;

    @Override
    public Optional<User> forgot(String email) {
        return userRepository.findByEmail(email).map(user -> {
            user.setResetPasswordCode(UUID.randomUUID().toString());
            String message = String.format(passwordResetMessage, user.getName(),
                    "https://sql-trainer.ru/password/reset?code=" + user.getResetPasswordCode());
            sendEmail(user.getEmail(), message, "Восстановление пароля");
            return userRepository.save(user);
        });
    }

    @Override
    public String registration(User user) {
        return userRepository.findByName(user.getName())
                .map(existingUser -> "Логин уже используется")
                .orElseGet(() -> userRepository.findByEmail(user.getEmail())
                        .map(existingEmailUser -> "Почта уже используется")
                        .orElseGet(() -> {
                            user.setPassword(passwordEncoder.encode(user.getPassword()));
                            user.setActivationCode(UUID.randomUUID().toString());
                            userRepository.save(user);

                            String activationLink = "https://sql-trainer.ru/signup/activate?code=" + user.getActivationCode();
                            String message = String.format(registrationMessage, user.getName(), activationLink);
                            sendEmail(user.getEmail(), message, "Подтверждение регистрации");
                            return "ok";
                        }));
    }

    public void sendTaskCompleteNotification(User teacher, User student, Task task) {
        sendEmail(teacher.getEmail(), String.format(taskCompleteMessage,
                teacher.getName(),
                student.getName(),
                task.getName()
        ), task.getName());
    }

    private void sendEmail(String to, String text, String subject) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("urazov.buj@gmail.com");
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);
        mailSender.send(mailMessage);
    }
}
