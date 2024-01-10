package ru.quinsis.sqltrainer.service.impl;

import ru.quinsis.sqltrainer.model.mongodb.Task;
import ru.quinsis.sqltrainer.repository.TaskRepository;
import ru.quinsis.sqltrainer.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;

    @Override
    public Optional<Task> getTaskById(String taskId) {
        return taskRepository.getTaskById(taskId);
    }

    @Override
    public Optional<Task> getTaskByCode(String code) {
        return taskRepository.getTaskByCode(code);
    }

    @Override
    public Optional<List<Task>> getIncompleteTasksByUserId(Long userId) {
        return taskRepository.getIncompleteTasksByUserId(userId);
    }

    @Override
    public Optional<List<Task>> getCompleteTasksByUserId(Long userId) {
        return taskRepository.getCompleteTasksByUserId(userId);
    }

    @Override
    public Optional<List<Task>> getTasksByOwnerId(Long id) {
        return taskRepository.getTasksByOwnerId(id);
    }

    public Task save(Task task) {
        return taskRepository.save(task);
    }

    public void delete(Task task) {
        taskRepository.delete(task);
    }
}
