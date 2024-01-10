package ru.quinsis.sqltrainer.controller;

import ru.quinsis.sqltrainer.model.postgres.User;
import ru.quinsis.sqltrainer.service.impl.UserMailServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequiredArgsConstructor
public class UserMailController {
    private final UserMailServiceImpl userMailService;

    @GetMapping("/signup")
    public ModelAndView signup() {
        return new ModelAndView("signup");
    }

    @PostMapping("/signup")
    public ModelAndView signup(User user, Model model, RedirectAttributes redirectAttributes) {
        String response = userMailService.registration(user);

        if (response.equals("ok")) {
            redirectAttributes.addFlashAttribute("activateMessage",
                    "На вашу почту отправлено письмо с подтверждением.");
            redirectAttributes.addFlashAttribute("activateMessageType", "alert");
            return new ModelAndView("redirect:/login");
        } else {
            model.addAttribute("errorMessage", response);
            model.addAttribute("failed", true);
            model.addAttribute("account", user);
            return new ModelAndView("signup");
        }
    }

    @GetMapping("/password/forgot")
    public ModelAndView forgot() {
        return new ModelAndView("forgotPassword");
    }

    @PostMapping("/password/forgot")
    public ModelAndView forgot(
            @RequestParam("email") String email,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (userMailService.forgot(email).isPresent()) {
            redirectAttributes.addFlashAttribute("activateMessage",
                    "На вашу почту отправлено письмо с инструкцией по восстановлению пароля.");
            redirectAttributes.addFlashAttribute("activateMessageType", "alert");
            return new ModelAndView("redirect:/password/forgot");
        } else {
            model.addAttribute("errorMessage", "Аккаунт с такой почтой не существует.");
            model.addAttribute("failed", true);
            model.addAttribute("email", email);
            return new ModelAndView("forgotPassword");
        }
    }
}
