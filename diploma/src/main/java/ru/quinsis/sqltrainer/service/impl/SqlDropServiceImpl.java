package ru.quinsis.sqltrainer.service.impl;

import ru.quinsis.sqltrainer.service.SqlDropService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Service
@RequiredArgsConstructor
public class SqlDropServiceImpl implements SqlDropService {
    private final DataSource dataSource;

    @Transactional
    public void rollback(String schemaId, String username) {
        dropSchema(schemaId);
        dropUser(username);
    }

    @Transactional
    public String dropUser(String userName) {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP USER " + userName);
                return "ok";
            } catch (SQLException e) {
                return e.getMessage();
            }
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    @Transactional
    public String dropSchema(String id) {
        // Заходим под пользователем с ограниченными правами
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("drop schema schema_" + id + " cascade");
            }
            return "ok";
        } catch (SQLException e) {
            return e.getMessage();
        }
    }
}
