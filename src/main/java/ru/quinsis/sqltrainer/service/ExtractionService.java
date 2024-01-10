package ru.quinsis.sqltrainer.service;

import ru.quinsis.sqltrainer.model.mongodb.Column;
import ru.quinsis.sqltrainer.model.mongodb.Table;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface ExtractionService {
    @Transactional
    void extractValuesFromTable(Connection connection, String schemaId, String tableName, Table table) throws SQLException;
    @Transactional
    List<Column> extractColumnsFromTable(Connection connection, String schemaId, String tableName, List<Column> columns) throws SQLException;
    @Transactional
    void extractMongoSchema(Connection connection, String schemaId, List<Table> newTables, ResultSet resultSet) throws SQLException;
}
