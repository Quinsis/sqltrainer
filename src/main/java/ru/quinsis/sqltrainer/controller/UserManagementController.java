package ru.quinsis.sqltrainer.controller;

import ru.quinsis.sqltrainer.model.postgres.User;
import ru.quinsis.sqltrainer.service.impl.UserManagementServiceImpl;
import ru.quinsis.sqltrainer.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequiredArgsConstructor
public class UserManagementController {
    private final UserManagementServiceImpl userManagementService;
    private final UserServiceImpl userService;

    @GetMapping("/signup/activate")
    public ModelAndView activate(
            @Param(value = "code") String code,
            RedirectAttributes redirectAttributes
    ) {
        if (userManagementService.activate(code).isPresent()) {
            redirectAttributes.addFlashAttribute("activateMessage", "Учётная запись успешно активирована.");
            redirectAttributes.addFlashAttribute("activateMessageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("activateMessage", "Неверный код подтверждения.");
            redirectAttributes.addFlashAttribute("activateMessageType", "deny");
        }
        return new ModelAndView("redirect:/login");
    }

    @PostMapping("/password/reset")
    public ModelAndView reset(
            User user, @Param(value = "code") String code,
            RedirectAttributes redirectAttributes
    ) {
        if (userManagementService.reset(code, user.getPassword()).isPresent()) {
            redirectAttributes.addFlashAttribute("activateMessage", "Пароль успешно изменён.");
            redirectAttributes.addFlashAttribute("activateMessageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("activateMessage", "Неверный код восстановления.");
            redirectAttributes.addFlashAttribute("activateMessageType", "deny");
        }
        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/password/reset")
    public ModelAndView reset(
            @Param(value = "code") String code,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        return userService.getUserByResetPasswordCode(code).map(user -> {
            model.addAttribute("code", code);
            return new ModelAndView("resetPassword");
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("activateMessage", "Неверный код восстановления.");
            redirectAttributes.addFlashAttribute("activateMessageType", "deny");
            return new ModelAndView("redirect:/password/forgot");
        });
    }
}
