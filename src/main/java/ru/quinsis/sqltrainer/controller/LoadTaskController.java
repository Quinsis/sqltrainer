package ru.quinsis.sqltrainer.controller;

import org.springframework.web.bind.annotation.PutMapping;
import ru.quinsis.sqltrainer.model.mongodb.Schema;
import ru.quinsis.sqltrainer.model.mongodb.Task;
import ru.quinsis.sqltrainer.model.mongodb.TaskConnection;
import ru.quinsis.sqltrainer.service.impl.TaskServiceImpl;
import ru.quinsis.sqltrainer.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.quinsis.sqltrainer.Utils;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class LoadTaskController {
    private final UserServiceImpl userService;
    private final TaskServiceImpl taskService;
    private final Utils utils;
    @Value("${spring.html.taskInfo}")
    private String taskInfoHtml;
    @Value("${spring.html.userTask}")
    private String taskHtml;
    @Value("${spring.html.userTaskInfo}")
    private String userTaskInfo;
    @Value("${spring.html.userTaskDescription}")
    private String userTaskDescription;

    @PostMapping("/loadTasks")
    public ResponseEntity<Map<String, String>> loadTasks(Principal principal) {
        try {
            Long userId = userService.getUserByName(principal.getName()).get().getId();

            List<Task> tasks = new ArrayList<>();
            taskService.getTasksByOwnerId(userId).ifPresent(list -> {
                tasks.addAll(list);
                Collections.reverse(tasks);
            });

            Map<String, String> responseData = new HashMap<>();
            StringBuilder tasksHtml = new StringBuilder();
            for (Task task : tasks) {
                tasksHtml.append("<li onclick='chooseTask(this)' class='task' id='").append(task.getId()).append("'>")
                        .append("<i class='fa-solid fa-book'></i>")
                        .append("<span>").append(task.getName()).append("</span>")
                        .append("<div class='taskActions'>")
                        .append("<svg onclick='deleteTask(this)' id='taskDelete' stroke='currentColor' fill='none' stroke-width='2' viewBox='0 0 24 24' stroke-linecap='round' stroke-linejoin='round' class='icon-sm' height='1em' width='1em' xmlns='http://www.w3.org/2000/svg'><polyline points='3 6 5 6 21 6'></polyline><path d='M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2'></path><line x1='10' y1='11' x2='10' y2='17'></line><line x1='14' y1='11' x2='14' y2='17'></line></svg>")
                        .append("</div>")
                        .append("</li>");
            }

            responseData.put("tasks", tasksHtml.toString());
            return new ResponseEntity<>(responseData, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Ошибка при загрузке схем: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/loadTaskById")
    public ResponseEntity<Map<String, String>> loadTaskById(@RequestParam("taskId") String taskId) {
        try {
            Task task = taskService.getTaskById(taskId).get();
            Schema schema = task.getSchema();
            Map<String, String> responseData = new HashMap<>();
            String tablesHtml = "<ul class='tables'>";
            tablesHtml += utils.loadTablesInternal(schema);
            String taskHtml = String.format(
                    taskInfoHtml, task.getCode(), task.getName(), task.getDescription(), task.getRequiredQuery(), tablesHtml);
            responseData.put("task", taskHtml);
            return new ResponseEntity<>(responseData, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Ошибка при загрузке задания: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/loadUserTaskByCode")
    public ResponseEntity<Map<String, String>> loadUserTaskByCode(String code) {
        Map<String, String> responseData = new HashMap<>();
        return taskService.getTaskByCode(code).map(task -> {
                    StringBuilder userTaskDescriptionHtml = new StringBuilder();
                    userTaskDescriptionHtml.append(String.format(userTaskDescription,
                            task.getName(),
                            task.getDescription(),
                            utils.loadTablesInternal(task.getSchema())
                    ));
                    responseData.put("userTaskInfo", String.format(userTaskInfo, userTaskDescriptionHtml));
                    return new ResponseEntity<>(responseData, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    responseData.put("description", "");
                    return new ResponseEntity<>(responseData, HttpStatus.OK);
                });
    }

    @PostMapping("/loadIncompleteUserTasks")
    public ResponseEntity<Map<String, String>> loadIncompleteUserTasks(Principal principal) {
        Map<String, String> responseData = new HashMap<>();
        Optional<List<Task>> incompleteTasks = taskService.getIncompleteTasksByUserId(userService.getUserByName(principal.getName()).get().getId());
        if (incompleteTasks.isPresent()) {
            Collections.reverse(incompleteTasks.get());
            StringBuilder tasksHtml = new StringBuilder();
            incompleteTasks.get().stream()
                    .filter(task -> task.getTaskConnections().stream().anyMatch(taskConnection -> !taskConnection.isCompleted()))
                    .collect(Collectors.toList()).forEach(task -> {
                        tasksHtml.append(String.format(taskHtml, task.getCode(), task.getName()));
                    });
            responseData.put("tasks", tasksHtml.toString());
        }
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @PostMapping("/loadCompleteUserTasks")
    public ResponseEntity<Map<String, String>> loadCompleteUserTasks(Principal principal) {
        Map<String, String> responseData = new HashMap<>();
        Optional<List<Task>> incompleteTasks = taskService.getCompleteTasksByUserId(userService.getUserByName(principal.getName()).get().getId());
        if (incompleteTasks.isPresent()) {
            Collections.reverse(incompleteTasks.get());
            StringBuilder tasksHtml = new StringBuilder();
            incompleteTasks.get().stream()
                    .filter(task -> task.getTaskConnections().stream().anyMatch(taskConnection -> taskConnection.isCompleted()))
                    .collect(Collectors.toList()).forEach(task -> tasksHtml.append(String.format(taskHtml, task.getCode(), task.getName())));
            responseData.put("tasks", tasksHtml.toString());
        }
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }
}
