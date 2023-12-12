package ru.quinsis.sqltrainer.service.impl;

import ru.quinsis.sqltrainer.model.mongodb.Schema;
import ru.quinsis.sqltrainer.repository.SchemaRepository;
import ru.quinsis.sqltrainer.service.SchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SchemaServiceImpl implements SchemaService {
    private final SchemaRepository schemaRepository;
    @Override
    public Optional<List<Schema>> getSchemasByUserId(Long userId) {
        return schemaRepository.getSchemasByUserId(userId);
    }

    @Override
    public Optional<Schema> getSchemaById(String id) {
        return schemaRepository.getSchemaById(id);
    }

    @Override
    public Schema save(Schema schema) {
        return schemaRepository.save(schema);
    }

    @Override
    public void delete(Schema schema) {
        schemaRepository.delete(schema);
    }
}
