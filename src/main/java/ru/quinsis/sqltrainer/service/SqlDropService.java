package ru.quinsis.sqltrainer.service;

import org.springframework.transaction.annotation.Transactional;

public interface SqlDropService {
    @Transactional
    void rollback(String schemaId);

    @Transactional
    String dropUser(String userName);

    @Transactional
    String dropSchema(String id);
}
