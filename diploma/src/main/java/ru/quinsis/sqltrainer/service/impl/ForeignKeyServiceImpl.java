package ru.quinsis.sqltrainer.service.impl;

import ru.quinsis.sqltrainer.model.mongodb.Column;
import ru.quinsis.sqltrainer.model.mongodb.Schema;
import ru.quinsis.sqltrainer.model.mongodb.Table;
import ru.quinsis.sqltrainer.service.ForeignKeyService;
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
public class ForeignKeyServiceImpl implements ForeignKeyService {
    private final InsertionServiceImpl insertionService;

    @Override
    @Transactional
    public void checkForeignKeys(Connection connection, Schema schema) throws SQLException {
        for (Table table : schema.getTables()) {
            for (Column column : table.getColumns()) {
                if (column.getForeignKeys() != null) {
                    addForeignKeys(connection, schema, table, column);
                }
            }
        }
    }

    @Override
    @Transactional
    public void addForeignKeys(Connection connection, Schema schema, Table table, Column column) throws SQLException {
        for (Map.Entry<String, List<String>> entry : column.getForeignKeys().entrySet()) {
            String referenceTable = entry.getKey();
            List<String> referenceColumns = entry.getValue();

            for (String referenceColumn : referenceColumns) {
                try (Statement statement = connection.createStatement()) {
                    String foreignKeyConstraint = String.format("fk_%s_%s", column.getName(), table.getName());
                    String query = String.format("alter table schema_%s.%s " +
                                    "add constraint %s foreign key (%s) " +
                                    "references schema_%s.%s(%s)",
                            schema.getId(), table.getName(), foreignKeyConstraint, column.getName(),
                            schema.getId(), referenceTable, referenceColumn);

                    statement.execute(query);
                }
            }
        }
    }

    @Override
    @Transactional
    public void setTablesWithForeignKeys(Connection connection, Schema schema, List<Table> tableWithForeignKeys)
            throws SQLException {
        for (Table table : schema.getTables()) {
            if (table.getColumns().stream().anyMatch(column -> column.getKeyStatus() == Column.KeyStatus.FOREIGN)) {
                tableWithForeignKeys.add(table);
            } else {
                insertionService.insertValuesIntoTable(connection, schema, table);
            }
        }
    }

    @Transactional
    public void extractForeignKeysFromTables(Connection connection, String schemaId, List<Table> tables) throws SQLException {
        for (Table table : tables) {
            for (Column column : table.getColumns()) {
                try (Statement statement1 = connection.createStatement()) {
                    Map<String, List<String>> foreignKeys = new HashMap<>();

                    String query = "SELECT \n" +
                            "    confrelid::regclass AS referenced_table,\n" +
                            "    af.attname AS referenced_column\n" +
                            "FROM \n" +
                            "    pg_constraint\n" +
                            "JOIN \n" +
                            "    pg_attribute AS a ON a.attnum = ANY(conkey) AND a.attrelid = conrelid\n" +
                            "JOIN \n" +
                            "    pg_attribute AS af ON af.attnum = ANY(confkey) AND af.attrelid = confrelid\n" +
                            "WHERE \n" +
                            "    confrelid IS NOT NULL AND\n" +
                            "    a.attname = '" + column.getName() + "'";

                    extractedForeignKeys(schemaId, table, column, statement1, foreignKeys, query);
                }
            }
        }
    }

    private void extractedForeignKeys(String schemaId, Table table, Column column, Statement statement1, Map<String, List<String>> foreignKeys, String query) throws SQLException {
        try (ResultSet foreignKeysRow = statement1.executeQuery(query)) {
            while (foreignKeysRow.next()) {
                String referencedTable = foreignKeysRow
                        .getObject(1)
                        .toString()
                        .replace("schema_" + schemaId + ".", "");

                String referencedColumn = foreignKeysRow
                        .getObject(2)
                        .toString();

                if (!referencedTable.equals(table.getName())) {
                    if (!foreignKeys.containsKey(referencedTable)) {
                        foreignKeys.put(referencedTable, new ArrayList<>());
                    }
                    foreignKeys.get(referencedTable).add(referencedColumn);
                }
            }

            if (!foreignKeys.isEmpty()) {
                column.setKeyStatus(Column.KeyStatus.FOREIGN);
            }

            column.setForeignKeys(foreignKeys);
        }
    }
}
