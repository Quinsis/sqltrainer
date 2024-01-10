package ru.quinsis.sqltrainer.service.impl;

import ru.quinsis.sqltrainer.model.mongodb.Schema;
import ru.quinsis.sqltrainer.repository.SchemaRepository;
import ru.quinsis.sqltrainer.service.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QueryServiceImpl implements QueryService {
    private final SchemaRepository schemaRepository;
    private final MigrationServiceImpl migrationService;
    private final SqlDropServiceImpl dropService;
    @Value("${spring.datasource.url}")
    private String dbUrl;
    private final DataSource dataSource;

    @Transactional
    public Optional<String> completeQuery(String sqlQuery, Schema schema) {
        return migrationService.mongoToPostgres(schema)
                .map(message -> message.equals("ok") ?
                        executeQuery(sqlQuery)
                                .map(queryResult ->
                                        queryResult.equalsIgnoreCase("Успешно: запрос не вернул результатов.") ?
                                                "Успешно: запрос не вернул результатов." : queryResult)
                                .orElse("В таблице отсутствуют записи.")
                        : message);
    }

    @Transactional
    public String getSavedQueryResult(String sqlQuery, Schema schema) {
        String queryResult = completeQuery(sqlQuery, schema).get();
        Schema newSchema = migrationService.postgresToMongo(schema);
        schemaRepository.save(newSchema);
        dropService.rollback(newSchema.getId());
        return queryResult;
    }

    @Transactional
    public String getUnsavedQueryResult(String sqlQuery, Schema schema) {
        String queryResult = completeQuery(sqlQuery, schema).get();
        dropService.rollback(schema.getId());
        return queryResult;
    }

    @Transactional
    public Optional<String> executeQuery(String sqlQuery) {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                String query = "";
                if (sqlQuery.toLowerCase().startsWith("select")) {
                    query = "select json_agg(data) from (" + sqlQuery + ") as data";
                } else {
                    query = sqlQuery;
                }

                if (query.toLowerCase().contains("public")) {
                    return Optional.of("ERROR: слово 'public' является системным, его нельзя использовать");
                }

                try {
                    ResultSet result = statement.executeQuery(query);
                    while (result.next()) {
                        return Optional.ofNullable(result.getString(1));
                    }
                    return Optional.of("Успешно: запрос не вернул результатов.");
                } catch (SQLException e) {
                    return Optional.ofNullable(e.getMessage());
                }
            }
        } catch (SQLException e) {
            return Optional.of("ERROR: " + e.getMessage());
        }
    }
}