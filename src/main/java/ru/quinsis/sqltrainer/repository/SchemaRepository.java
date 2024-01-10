package ru.quinsis.sqltrainer.repository;

import ru.quinsis.sqltrainer.model.mongodb.Schema;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchemaRepository extends MongoRepository<Schema, String> {
    @Query("{ 'userId' : ?0 }")
    Optional<List<Schema>> getSchemasByUserId(Long userId);

    @Query("{ 'id' : ?0 }")
    Optional<Schema> getSchemaById(String id);
}
