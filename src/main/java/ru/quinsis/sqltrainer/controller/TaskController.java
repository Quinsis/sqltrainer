package ru.quinsis.sqltrainer.controller;

import org.springframework.beans.factory.annotation.Value;
import ru.quinsis.sqltrainer.Utils;
import ru.quinsis.sqltrainer.model.mongodb.Schema;
import ru.quinsis.sqltrainer.model.mongodb.Task;
import ru.quinsis.sqltrainer.model.mongodb.TaskAttempt;
import ru.quinsis.sqltrainer.model.mongodb.TaskConnection;
import ru.quinsis.sqltrainer.model.postgres.User;
import ru.quinsis.sqltrainer.service.impl.SchemaServiceImpl;
import ru.quinsis.sqltrainer.service.impl.TaskServiceImpl;
import ru.quinsis.sqltrainer.service.impl.UserMailServiceImpl;
import ru.quinsis.sqltrainer.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class TaskController {
    private final TaskServiceImpl taskService;
    private final UserServiceImpl userService;
    private final SchemaServiceImpl schemaService;
    private final UserMailServiceImpl userMailService;
    private final Utils utils;
    @Value("${spring.html.userTaskAttempt}")
    private String userTaskAttempt;
    @Value("${spring.html.taskResult}")
    private String taskResult;

    @DeleteMapping("/deleteTask")
    public ResponseEntity<String> deleteTask(@RequestParam("taskId") String taskId) {
        try {
            Task task = taskService.getTaskById(taskId).get();
            taskService.delete(task);
            return ResponseEntity.ok("Схема успешно удалена.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при удалении схемы: " + e.getMessage());
        }
    }

    @PutMapping("/createTask")
    public ResponseEntity<Map<String, String>> createTask(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("query") String query,
            @RequestParam(name = "schemaId", required = false) String schemaId,
            @RequestParam(name = "taskId", required = false) String taskId,
            Principal principal) {
        try {
            Task task = new Task();
            if (taskId != null) {
                task = taskService.getTaskById(taskId).get();
                task.setName(name);
                task.setDescription(description);
                task.setRequiredQuery(query);
            } else if (schemaId != null) {
                Long userId = userService.getUserByName(principal.getName()).get().getId();
                Schema schema = schemaService.getSchemaById(schemaId).get();
                task.setOwnerId(userId);
                task.setSchema(schema);
                task.setName(name);
                task.setDescription(description);
                task.setRequiredQuery(query);
                task.setCode(UUID.randomUUID().toString());
                task.setTaskConnections(new ArrayList<>());
            }
            taskService.save(task);
            Map<String, String> responseData = new HashMap<>();
            return new ResponseEntity<>(responseData, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Ошибка при создании задания: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/join")
    public ResponseEntity<Map<String, String>> join(@RequestParam("code") String code, Principal principal) {
        Map<String, String> responseData = new HashMap<>();
        Long userId = userService.getUserByName(principal.getName()).get().getId();

        return taskService.getTaskByCode(code).map(task -> task.getTaskConnections()
                .stream()
                .filter(taskConnection -> taskConnection.getUserId() == userId)
                .findFirst().map(taskConnection -> {
                    responseData.put("message", "Вы уже присоединены к заданию");
                    return new ResponseEntity<>(responseData, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    task.getTaskConnections().add(TaskConnection.builder().attempts(new ArrayList<>()).userId(userId).isCompleted(false).build());
                    taskService.save(task);
                    responseData.put("message", "Задание получено");
                    return new ResponseEntity<>(responseData, HttpStatus.OK);
                }))
                .orElseGet(() -> {
                    responseData.put("message", "Задание не найдено");
                    return new ResponseEntity<>(responseData, HttpStatus.OK);
                });
    }

    @PostMapping("/checkTaskQuery")
    public String checkTaskQuery(
            @RequestParam("code") String code, @RequestParam("sqlQuery") String sqlQuery, Principal principal) {
        Long userId = userService.getUserByName(principal.getName()).get().getId();
        return taskService.getTaskByCode(code).map(task -> task.getTaskConnections()
                        .stream()
                        .filter(taskConnection -> taskConnection.getUserId() == userId)
                        .findFirst().map(taskConnection -> {
                            String requiredQuery = task.getRequiredQuery();
                            String userQuery = sqlQuery;
                            if (requiredQuery.charAt(requiredQuery.length() - 1) == ';') {
                                requiredQuery = requiredQuery.substring(0, requiredQuery.length() - 1);
                            }
                            if (userQuery.charAt(userQuery.length() - 1) == ';') {
                                userQuery = userQuery.substring(0, userQuery.length() - 1);
                            }
                            if (requiredQuery.equalsIgnoreCase(userQuery)) {
                                return "Поздравляем, ответ верный";
                            } else {
                                return "К сожалению, ответ неверный";
                            }
                        }).orElse("Ошибка, обновите страницу"))
                .orElse("Ошибка, обновите страницу");
    }

    @PostMapping("/getUserTaskAttempts")
    public ResponseEntity<Map<String, String>> checkTaskQuery(@RequestParam("code") String code, Principal principal) {
        Long userId = userService.getUserByName(principal.getName()).get().getId();
        Map<String, String> responseData = new HashMap<>();
        return taskService.getTaskByCode(code).map(task -> task.getTaskConnections()
                        .stream()
                        .filter(taskConnection -> taskConnection.getUserId() == userId)
                        .findFirst().map(taskConnection -> {
                            StringBuilder userTaskAttemptsHtml = new StringBuilder();
                            Collections.reverse(taskConnection.getAttempts());
                            taskConnection.getAttempts().forEach(taskAttempt ->
                                    userTaskAttemptsHtml.append(String.format(userTaskAttempt,
                                            taskAttempt.getStatus().toString().toLowerCase(),
                                            taskAttempt.getStatus(),
                                            taskAttempt.getQuery(),
                                            taskAttempt.getDate().toString())
                                    )
                            );
                            responseData.put("attempts", userTaskAttemptsHtml.toString());
                            return new ResponseEntity<>(responseData, HttpStatus.OK);
                        }).orElseGet(() -> {
                            responseData.put("attempts", "");
                            return new ResponseEntity<>(responseData, HttpStatus.OK);
                        }))
                    .orElseGet(() -> {
                        responseData.put("attempts", "");
                        return new ResponseEntity<>(responseData, HttpStatus.OK);
                    });
    }

    @PostMapping("/getTaskResults")
    public ResponseEntity<Map<String, String>> getTaskResults(@RequestParam("code") String code) {
        Map<String, String> responseData = new HashMap<>();
        return taskService.getTaskById(code).map(task -> {
            StringBuilder taskResultsHtml = new StringBuilder();
            task.getTaskConnections()
                    .stream()
                    .filter(taskConnection -> taskConnection.isCompleted())
                    .collect(Collectors.toList()).forEach(taskConnection ->
                            taskResultsHtml.append(String.format(taskResult,
                            taskConnection.getAttempts().stream()
                                    .filter(taskAttempt -> taskAttempt.getStatus() == TaskAttempt.Status.ACCEPTED)
                                    .findFirst().map(taskAttempt -> taskAttempt.getStatus().toString().toLowerCase())
                                    .orElse("error"),
                            taskConnection.getAttempts().stream()
                                    .filter(taskAttempt -> taskAttempt.getStatus() == TaskAttempt.Status.ACCEPTED)
                                    .findFirst().map(taskAttempt -> TaskAttempt.Status.ACCEPTED)
                                    .orElse(TaskAttempt.Status.ERROR),
                            userService.getUserById(taskConnection.getUserId()).get().getName(),
                            taskConnection.getAttempts().stream()
                                    .filter(taskAttempt -> taskAttempt.getStatus() == TaskAttempt.Status.ACCEPTED)
                                    .findFirst().map(taskAttempt -> taskAttempt.getQuery())
                                    .orElse(""),
                            taskConnection.getAttempts().stream()
                                    .filter(taskAttempt -> taskAttempt.getStatus() == TaskAttempt.Status.ACCEPTED)
                                    .findFirst().map(taskAttempt -> taskAttempt.getDate().toString())
                                    .orElse("--:--:--")
                    )));
                    responseData.put("taskResults", taskResultsHtml.toString());
                    return new ResponseEntity<>(responseData, HttpStatus.OK);
        }).orElseGet(() -> {
            responseData.put("taskResults", "");
            return new ResponseEntity<>(responseData, HttpStatus.OK);
        });
    }

    @PutMapping("/addUserAttempt")
    public void addUserAttempt(
            @RequestParam("code") String code,
            @RequestParam("sqlQuery") String sqlQuery,
            @RequestParam("status") String status, Principal principal) {
        taskService.getTaskByCode(code).map(task -> task.getTaskConnections().stream()
                .filter(taskConnection -> taskConnection.getUserId() ==
                        userService.getUserByName(principal.getName()).get().getId())
                .findFirst().map(taskConnection -> {
                    taskConnection.getAttempts().add(
                            TaskAttempt.builder()
                                    .date(Date.from(Instant.now()))
                                    .status(status.equals("Поздравляем, ответ верный") ?
                                            TaskAttempt.Status.ACCEPTED : TaskAttempt.Status.ERROR)
                                    .query(sqlQuery)
                                    .build());
                    return taskService.save(task);
                })
        );
    }

    @PostMapping("/setCompletedTask")
    public void setCompletedTask(@RequestParam("code") String code, @RequestParam("isCompleted") boolean isCompleted, Principal principal) {
        taskService.getTaskByCode(code).map(task ->
                task.getTaskConnections().stream().filter(taskConnection ->
                    taskConnection.getUserId() == userService.getUserByName(principal.getName()).get().getId()
                ).findFirst().map(taskConnection -> {
                    taskConnection.setCompleted(isCompleted);
                    return taskService.save(task);
                }));
    }

    @PostMapping("/sendTaskCompleteNotification")
    public void sendTaskCompleteNotification(@RequestParam("code") String code, Principal principal) {
        userMailService.sendTaskCompleteNotification(
                userService.getUserById(taskService.getTaskByCode(code).get().getOwnerId()).get(),
                userService.getUserByName(principal.getName()).get(),
                taskService.getTaskByCode(code).get()
        );
    }
}
