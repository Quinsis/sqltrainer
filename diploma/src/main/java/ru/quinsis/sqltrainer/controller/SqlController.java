package ru.quinsis.sqltrainer.controller;

import ru.quinsis.sqltrainer.service.impl.QueryServiceImpl;
import ru.quinsis.sqltrainer.service.impl.SchemaServiceImpl;
import ru.quinsis.sqltrainer.service.impl.TaskServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.quinsis.sqltrainer.model.mongodb.Schema;

@RestController
@RequiredArgsConstructor
public class SqlController {
    private final SchemaServiceImpl schemaService;
    private final TaskServiceImpl taskService;
    private final QueryServiceImpl apiService;

    @PostMapping("/executeQuery")
    public ResponseEntity<String> executeSqlQuery(
            @RequestParam("sqlQuery") String sqlQuery,
            @RequestParam("schemaId") String schemaId
    ) {
        try {
            Schema schema = schemaService.getSchemaById(schemaId).get();
            if (sqlQuery.charAt(sqlQuery.length() - 1) == ';') {
                sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 1);
            }
            return ResponseEntity.ok(executeSql(sqlQuery, schema));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }

    @PostMapping("/executeQueryWithoutSave")
    public ResponseEntity<String> executeSqlQueryWithoutSave(
            @RequestParam("sqlQuery") String sqlQuery,
            @RequestParam(name = "schemaId", required = false) String schemaId,
            @RequestParam(name = "taskId", required = false) String taskId,
            @RequestParam(name = "taskCode", required = false) String taskCode
    ) {
        try {
            Schema schema;

            if (schemaId.length() != 0) schema = schemaService.getSchemaById(schemaId).get();
            else if (taskId.length() != 0) schema = taskService.getTaskById(taskId).get().getSchema();
            else schema = taskService.getTaskByCode(taskCode).get().getSchema();

            if (sqlQuery.charAt(sqlQuery.length() - 1) == ';') {
                sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 1);
            }

            return ResponseEntity.ok(executeSqlWithoutSave(sqlQuery, schema));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }

    private String executeSqlWithoutSave(String sqlQuery, Schema schema) {
        return apiService.getUnsavedQueryResult(sqlQuery, schema);
    }

    private String executeSql(String sqlQuery, Schema schema) {
        return apiService.getSavedQueryResult(sqlQuery, schema);
    }
}
