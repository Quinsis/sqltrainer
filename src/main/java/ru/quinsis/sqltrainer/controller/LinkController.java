package ru.quinsis.sqltrainer.controller;

import ru.quinsis.sqltrainer.model.mongodb.OfflineLink;
import ru.quinsis.sqltrainer.model.mongodb.OnlineLink;
import ru.quinsis.sqltrainer.model.mongodb.Schema;
import ru.quinsis.sqltrainer.service.impl.LinkServiceImpl;
import ru.quinsis.sqltrainer.service.impl.SchemaServiceImpl;
import ru.quinsis.sqltrainer.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class LinkController {
    private final LinkServiceImpl linkService;
    private final SchemaServiceImpl schemaService;
    private final UserServiceImpl userService;

    @GetMapping("/online/{code}")
    public ModelAndView online(@PathVariable("code") String code, Principal principal) {
        return linkService.getOnlineLinkByCode(code)
                .map(link -> {
                    String schemaId = link.getSchemaId();
                    Schema schema = schemaService.getSchemaById(schemaId).get();
                    return importSchema(schema, principal);
                }).orElse(new ModelAndView("error"));
    }

    @GetMapping("/offline/{code}")
    public ModelAndView offline(@PathVariable("code") String code, Principal principal) {
        return linkService.getOfflineLinkByCode(code)
                .map(link -> {
                    Schema schema = link.getSchema();
                    return importSchema(schema, principal);
                }).orElseGet(() -> new ModelAndView("error"));
    }

    @PostMapping("/link/online")
    public String createOnlineLink(@RequestBody String schemaId) {
        return "https://sql-trainer.ru/online/" + linkService.getOnlineLinkBySchemaId(schemaId.substring(0, schemaId.length() - 1))
                .map(link -> link.getCode())
                .orElseGet(() -> {
                    OnlineLink onlineLink = new OnlineLink();
                    onlineLink.setCode(UUID.randomUUID().toString());
                    onlineLink.setSchemaId(schemaId.substring(0, schemaId.length() - 1));
                    linkService.saveOnlineLink(onlineLink);
                    return onlineLink.getCode();
                });
    }

    @PostMapping("/link/offline")
    public String createOfflineLink(@RequestBody String schemaId) {
        return "https://sql-trainer.ru/offline/" + schemaService.getSchemaById(schemaId.substring(0, schemaId.length() - 1))
                .map(schema -> {
                    OfflineLink offlineLink = new OfflineLink();
                    offlineLink.setCode(UUID.randomUUID().toString());
                    offlineLink.setSchema(schema);
                    linkService.saveOfflineLink(offlineLink);
                    return offlineLink.getCode();
                }).orElseGet(null);
    }

    @PutMapping("/importSchema")
    public ModelAndView importSchema(Schema schema, Principal principal) {
        Long userId = userService.getUserByName(principal.getName()).get().getId();
        schema.setId(null);
        schema.setUserId(userId);
        schema.setName("Импортированная схема");
        schemaService.save(schema);
        return new ModelAndView("redirect:/sandbox");
    }
}
