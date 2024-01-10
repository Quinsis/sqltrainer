package ru.quinsis.sqltrainer.service;

import ru.quinsis.sqltrainer.model.mongodb.Task;

import java.util.List;
import java.util.Optional;

public interface TaskService {
    Optional<List<Task>> getTasksByOwnerId(Long ownerId);
    Optional<Task> getTaskById(String taskId);
    Optional<Task> getTaskByCode(String code);
    Optional<List<Task>> getIncompleteTasksByUserId(Long userId);
    Optional<List<Task>> getCompleteTasksByUserId(Long userId);
    Task save(Task task);
    void delete(Task task);
}
