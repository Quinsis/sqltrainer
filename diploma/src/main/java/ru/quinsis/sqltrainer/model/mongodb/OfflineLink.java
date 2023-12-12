package ru.quinsis.sqltrainer.model.mongodb;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "offline links")
@Getter
@Setter
public class OfflineLink {
    @Id
    private String id;

    @Field(name = "schema")
    private Schema schema;

    @Field(name = "code")
    private String code;
}
