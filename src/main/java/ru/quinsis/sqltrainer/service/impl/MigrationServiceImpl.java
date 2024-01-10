package ru.quinsis.sqltrainer.service.impl;

import ru.quinsis.sqltrainer.model.mongodb.Schema;
import ru.quinsis.sqltrainer.model.mongodb.Table;
import ru.quinsis.sqltrainer.service.MigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MigrationServiceImpl implements MigrationService {
    private final DataSource dataSource;
    private final SqlCreationServiceImpl sqlCreationService;
    private final ForeignKeyServiceImpl foreignKeyService;
    private final InsertionServiceImpl insertionService;
    private final ExtractionServiceImpl extractionService;

    @Override
    @Transactional
    public Optional<String> mongoToPostgres(Schema schema) {
        try (Connection connection = dataSource.getConnection()) {
            sqlCreationService.createSchema(connection, schema);
            foreignKeyService.checkForeignKeys(connection, schema);

            List<Table> tableWithForeignKeys = new ArrayList<>();
            foreignKeyService.setTablesWithForeignKeys(connection, schema, tableWithForeignKeys);

            for (Table table : tableWithForeignKeys) {
                insertionService.insertValuesIntoTable(connection, schema, table);
            }
            return Optional.of("ok");
        } catch (SQLException e) {
            return Optional.ofNullable(e.getMessage());
        }
    }

    @Transactional
    public Schema postgresToMongo(Schema schema) {
        try (Connection connection = dataSource.getConnection()) {
            // Инициализируем новую схему
            String schemaId = schema.getId();
            Schema newSchema = new Schema();
            newSchema.setId(schemaId);
            newSchema.setUserId(schema.getUserId());
            newSchema.setName(schema.getName());

            return initMongoSchema(connection, schemaId, newSchema);
        } catch (SQLException e) {
            e.printStackTrace();
            return schema;
        }
    }

    @Transactional
    public Schema initMongoSchema(Connection connection, String schemaId, Schema newSchema) throws SQLException {
        // Заполняем массив таблиц
        try (Statement statement = connection.createStatement()) {
            List<Table> newTables = new ArrayList<>();

            try (ResultSet resultSet = statement.executeQuery("select table_name from information_schema.tables where table_schema = 'schema_" + schemaId + "'")) {
                extractionService.extractMongoSchema(connection, schemaId, newTables, resultSet);
            } finally {
                newSchema.setTables(newTables);
                return newSchema;
            }
        }
    }
}
