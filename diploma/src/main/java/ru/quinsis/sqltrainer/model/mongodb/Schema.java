package ru.quinsis.sqltrainer.model.mongodb;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "schemas")
@Getter
@Setter
public class Schema {
    @Id
    private String id;

    @Field(name = "name")
    private String name;

    @Field(name = "user_id")
    private Long userId;

    @Field(name = "tables")
    private List<Table> tables;
}
