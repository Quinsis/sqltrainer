package ru.quinsis.sqltrainer.service.impl;

import ru.quinsis.sqltrainer.model.mongodb.Schema;
import ru.quinsis.sqltrainer.repository.SchemaRepository;
import ru.quinsis.sqltrainer.service.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QueryServiceImpl implements QueryService {
    private final SchemaRepository schemaRepository;
    private final MigrationServiceImpl migrationService;
    private final SqlCreationServiceImpl sqlCreationService;
    private final SqlDropServiceImpl dropService;
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Transactional
    public Optional<String> completeQuery(String sqlQuery, Schema schema, String userName, String userPassword) {
        return migrationService.mongoToPostgres(schema)
                .map(message -> message.equals("ok") ?
                        sqlCreationService.createUser(userName, userPassword, "schema_" + schema.getId())
                                .map(privilegesMessage -> privilegesMessage.equals("ok") ?
                                        executeQuery(sqlQuery, userName, userPassword)
                                                .map(queryResult ->
                                                        queryResult.toLowerCase().endsWith("запрос не вернул результатов.") ?
                                                                "Успешно: " + queryResult : queryResult)
                                                .orElse("В таблице отсутствуют записи.")
                                        : privilegesMessage)
                                .orElse("Ошибка при настройке привилегий для пользователя.")
                        : message);
    }

    @Transactional
    public String getSavedQueryResult(String sqlQuery, Schema schema) {
        String queryResult = completeQuery(sqlQuery, schema, "user_" + schema.getUserId(), schema.getId()).get();
        Schema newSchema = migrationService.postgresToMongo(schema);
        schemaRepository.save(newSchema);
        dropService.rollback(newSchema.getId(), "user_" + schema.getUserId());
        return queryResult;
    }

    @Transactional
    public String getUnsavedQueryResult(String sqlQuery, Schema schema) {
        String queryResult = completeQuery(sqlQuery, schema, "user_" + schema.getUserId(), schema.getId()).get();
        dropService.rollback(schema.getId(), "user_" + schema.getUserId());
        return queryResult;
    }

    @Transactional
    public Optional<String> executeQuery(String sqlQuery, String userName, String userPassword) {
        try (Connection connection = DriverManager.getConnection(dbUrl, userName, userPassword)) {
            try (Statement statement = connection.createStatement()) {
                String query = "";
                if (sqlQuery.toLowerCase().startsWith("select")) {
                    query = "select json_agg(data) from (" + sqlQuery + ") as data";
                } else {
                    query = sqlQuery;
                }

                try {
                    ResultSet result = statement.executeQuery(query);
                    while (result.next()) {
                        return Optional.ofNullable(result.getString(1));
                    }
                    return Optional.of("Успешно: Запрос не вернул результатов.");
                } catch (SQLException e) {
                    return Optional.ofNullable(e.getMessage());
                }
            }
        } catch (SQLException e) {
            return Optional.of("Ошибка при выполнении SQL-запроса: " + e.getMessage());
        }
    }
}