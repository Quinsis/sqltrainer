package ru.quinsis.sqltrainer.service;

import ru.quinsis.sqltrainer.model.mongodb.Schema;
import ru.quinsis.sqltrainer.model.mongodb.Table;
import jakarta.transaction.Transactional;

import java.sql.Connection;
import java.sql.SQLException;

public interface InsertionService {
    @Transactional
    void insertValuesIntoTable(Connection connection, Schema schema, Table table) throws SQLException;
}
