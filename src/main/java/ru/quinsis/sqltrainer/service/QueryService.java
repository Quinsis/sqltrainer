package ru.quinsis.sqltrainer.service;

import ru.quinsis.sqltrainer.model.mongodb.Schema;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface QueryService {
    @Transactional
    Optional<String> completeQuery(String sqlQuery, Schema schema);
    @Transactional
    String getSavedQueryResult(String sqlQuery, Schema schema);
    @Transactional
    String getUnsavedQueryResult(String sqlQuery, Schema schema);
    @Transactional
    Optional<String> executeQuery(String sqlQuery);
}
