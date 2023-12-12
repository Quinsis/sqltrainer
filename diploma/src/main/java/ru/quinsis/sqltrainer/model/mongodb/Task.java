package ru.quinsis.sqltrainer.model.mongodb;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "tasks")
@Getter
@Setter
public class Task {
    @Id
    private String id;

    @Field(name = "owner_id")
    private Long ownerId;

    @Field(name = "schema")
    private Schema schema;

    @Field(name = "name")
    private String name;

    @Field(name = "description")
    private String description;

    @Field(name = "required_query")
    private String requiredQuery;

    @Field(name = "code")
    private String code;

    @Field(name = "connections")
    private List<TaskConnection> taskConnections;
}
