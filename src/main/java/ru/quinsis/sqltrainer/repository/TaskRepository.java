package ru.quinsis.sqltrainer.repository;

import ru.quinsis.sqltrainer.model.mongodb.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    @Query("{ 'ownerId' : ?0 }")
    Optional<List<Task>> getTasksByOwnerId(Long ownerId);

    @Query("{ 'id':  ?0 }")
    Optional<Task> getTaskById(String taskId);

    @Query("{'code':  ?0}")
    Optional<Task> getTaskByCode(String code);

    @Query("{'taskConnections.userId':  ?0}")
    Optional<List<Task>> getTasksByUserId(Long userId);

    @Query(value = "{ 'taskConnections': { $elemMatch: { 'userId': ?0, 'isCompleted': false } } }")
    Optional<List<Task>> getIncompleteTasksByUserId(Long userId);

    @Query(value = "{ 'taskConnections': { $elemMatch: { 'userId': ?0, 'isCompleted': true } } }")
    Optional<List<Task>> getCompleteTasksByUserId(Long userId);
}
