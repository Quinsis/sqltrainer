package ru.quinsis.sqltrainer.service.impl;

import ru.quinsis.sqltrainer.model.mongodb.Column;
import ru.quinsis.sqltrainer.model.mongodb.Schema;
import ru.quinsis.sqltrainer.model.mongodb.Table;
import ru.quinsis.sqltrainer.service.SqlCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SqlCreationServiceImpl implements SqlCreationService {
    private final DataSource dataSource;

    @Override
    @Transactional
    public void createTable(Connection connection, Schema schema, Table table) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(String.format("create table schema_%s.%s()", schema.getId(), table.getName()));
        }
    }

    @Override
    @Transactional
    public void createColumn(Connection connection, Schema schema, Table table, Column column) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(String.format("alter table schema_%s.%s add %s %s %s",
                    schema.getId(), table.getName(), column.getName(), column.getDataType(),
                    getColumnConstraints(column)));
        }
    }

    @Override
    @Transactional
    public void createSchema(Connection connection, Schema schema) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(String.format("create schema schema_%s", schema.getId()));
        }
        for (Table table : schema.getTables()) {
            createTable(connection, schema, table);
            for (Column column : table.getColumns()) {
                createColumn(connection, schema, table, column);
            }
        }
    }

    private String getColumnConstraints(Column column) {
        StringBuilder constraints = new StringBuilder();
        if (column.getKeyStatus() == Column.KeyStatus.PRIMARY) {
            constraints.append(" primary key");
        }
        if (!column.isNullable() && column.getKeyStatus() != Column.KeyStatus.PRIMARY) {
            constraints.append(" not null");
        }
        return constraints.toString();
    }

    @Transactional
    public Optional<String> createUser(String userName, String userPassword, String schemaName) {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE USER " + userName + " with password '" + userPassword + "'");
                statement.execute("REVOKE ALL PRIVILEGES ON DATABASE test FROM " + userName);
                statement.execute("ALTER SCHEMA " + schemaName + " OWNER TO " + userName);
                statement.execute("GRANT USAGE ON SCHEMA " + schemaName + " TO " + userName);
                statement.execute("GRANT ALL ON ALL TABLES IN SCHEMA " + schemaName + " TO " + userName);
                statement.execute("ALTER USER " + userName + " SET search_path = '" + schemaName + "'");
                statement.execute(
                        "DO $$ " +
                                "DECLARE " +
                                "   t_name text; " +
                                "BEGIN " +
                                "   FOR t_name IN (SELECT table_name FROM information_schema.tables " +
                                "WHERE table_schema = '" + schemaName + "') " +
                                "   LOOP " +
                                "      EXECUTE 'ALTER TABLE " + schemaName + ".' || t_name || ' OWNER TO " + userName + "'; " +
                                "   END LOOP; " +
                                "END $$;"
                );

                return Optional.of("ok");
            } catch (SQLException e) {
                return Optional.ofNullable(e.getMessage());
            }
        } catch (SQLException e) {
            return Optional.ofNullable(e.getMessage());
        }
    }
}
