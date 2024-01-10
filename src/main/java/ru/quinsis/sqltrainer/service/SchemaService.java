package ru.quinsis.sqltrainer.service;

import ru.quinsis.sqltrainer.model.mongodb.Schema;

import java.util.List;
import java.util.Optional;

public interface SchemaService {
    Optional<List<Schema>> getSchemasByUserId(Long userId);
    Optional<Schema> getSchemaById(String id);
    Schema save(Schema schema);
    void delete(Schema schema);
}
