package ru.quinsis.sqltrainer.service;

import ru.quinsis.sqltrainer.model.mongodb.Schema;
import jakarta.transaction.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface MigrationService {
    @Transactional
    Optional<String> mongoToPostgres(Schema schema);
    @Transactional
    Schema postgresToMongo(Schema schema);

    Schema initMongoSchema(Connection connection, String schemaId, Schema newSchema) throws SQLException;
}
