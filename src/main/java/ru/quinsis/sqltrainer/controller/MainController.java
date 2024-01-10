package ru.quinsis.sqltrainer.controller;

import ru.quinsis.sqltrainer.model.mongodb.Schema;
import ru.quinsis.sqltrainer.service.impl.SchemaServiceImpl;
import ru.quinsis.sqltrainer.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MainController {
    private final UserServiceImpl userService;
    private final SchemaServiceImpl schemaService;

    @GetMapping("/")
    public ModelAndView index(Model model, Principal principal) {
        if (principal != null) {
            userService.getUserByName(principal.getName()).ifPresent(user -> model.addAttribute("user", user));
        }

        if (LocalTime.now().isBefore(LocalTime.of(6, 0))) {
            model.addAttribute("greeting", "Доброй ночи");
        } else if (LocalTime.now().isBefore(LocalTime.NOON)) {
            model.addAttribute("greeting", "Доброе утро");
        } else if (LocalTime.now().isBefore(LocalTime.of(18, 0))) {
            model.addAttribute("greeting", "Добрый день");
        } else {
            model.addAttribute("greeting", "Добрый вечер");
        }

        return new ModelAndView("index");
    }

    @GetMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("login");
    }

    @GetMapping("/role")
    public ModelAndView index() {
        return new ModelAndView("role");
    }

    @GetMapping("/sandbox")
    public ModelAndView sandbox(Model model, Principal principal) {
        Long userId = userService.getUserByName(principal.getName()).get().getId();
        List<Schema> schemas = schemaService.getSchemasByUserId(userId).get();
        Collections.reverse(schemas);
        model.addAttribute("schemas", schemas);
        return new ModelAndView("sandbox");
    }

    @GetMapping("/student")
    public ModelAndView student() {
        return new ModelAndView("student");
    }

    @GetMapping("/teacher")
    public ModelAndView teacher() {
        return new ModelAndView("teacher");
    }

    @GetMapping("/error")
    public ModelAndView error() {
        return new ModelAndView("error");
    }
}
