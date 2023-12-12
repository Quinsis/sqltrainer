package ru.quinsis.sqltrainer.service.impl;

import ru.quinsis.sqltrainer.model.mongodb.Column;
import ru.quinsis.sqltrainer.model.mongodb.Table;
import ru.quinsis.sqltrainer.service.ExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExtractionServiceImpl implements ExtractionService {
    private final ForeignKeyServiceImpl foreignKeyService;
    @Transactional
    public void extractValuesFromTable(
            Connection connection,
            String schemaId,
            String tableName,
            Table table
    ) throws SQLException {
        try (Statement statement1 = connection.createStatement()) {
            try (ResultSet valueRow = statement1.executeQuery("select * from schema_" + schemaId + "." + tableName)) {
                while (valueRow.next()) {
                    Map<String, Object> values = new HashMap<>();
                    for (int i = 1; i <= valueRow.getMetaData().getColumnCount(); i++) {
                        String columnName = valueRow.getMetaData().getColumnName(i);
                        Object value = valueRow.getObject(i);
                        values.put(columnName, value);
                    }
                    table.getValues().add(values);
                }
            }
        }
    }

    @Transactional
    public List<Column> extractColumnsFromTable(
            Connection connection,
            String schemaId,
            String tableName,
            List<Column> columns
    ) throws SQLException {
        try (Statement statement1 = connection.createStatement()) {
            try (ResultSet columnRows = statement1.executeQuery("SELECT" +
                    "    column_name," +
                    "    data_type," +
                    "    is_nullable," +
                    "    CASE" +
                    "        WHEN column_name IN (" +
                    "            SELECT kcu.column_name" +
                    "            FROM information_schema.key_column_usage kcu" +
                    "            JOIN information_schema.table_constraints tc" +
                    "            ON kcu.constraint_name = tc.constraint_name" +
                    "            WHERE kcu.table_schema = 'schema_" + schemaId + "'" +
                    "                AND kcu.table_name = '" + tableName + "'" +
                    "                AND tc.constraint_type = 'PRIMARY KEY'" +
                    "        ) THEN 'YES'" +
                    "        ELSE 'NO'" +
                    "    END AS is_primary_key " +
                    "FROM information_schema.columns " +
                    "WHERE table_schema = 'schema_" + schemaId + "'" +
                    "    AND table_name = '" + tableName + "'")) {
                while (columnRows.next()) {
                    Column column = new Column();
                    column.setName(columnRows.getObject(1).toString());
                    column.setDataType(columnRows.getObject(2).toString());
                    column.setNullable(columnRows.getObject(3).toString().equals("YES"));
                    column.setKeyStatus(columnRows.getObject(4).toString().equals("YES")
                            ? Column.KeyStatus.PRIMARY : Column.KeyStatus.NONE);
                    column.setForeignKeys(new HashMap<>());
                    columns.add(column);
                }
                return columns;
            }
        }
    }

    @Transactional
    public void extractMongoSchema(Connection connection, String schemaId, List<Table> newTables, ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            Table newTable = new Table();
            newTable.setName(resultSet.getString("table_name"));
            newTable.setValues(new ArrayList<>());

            // Вытаскиваем данные о записях таблицы
            extractValuesFromTable(connection, schemaId, resultSet.getString("table_name"), newTable);

            // Вытаскиваем данные о колонках таблицы
            newTable.setColumns(extractColumnsFromTable(connection, schemaId, resultSet.getString("table_name"), new ArrayList<>()));
            newTables.add(newTable);
        }

        // Вытаскиваем данные о внешних ключах таблиц
        foreignKeyService.extractForeignKeysFromTables(connection, schemaId, newTables);
    }
}
