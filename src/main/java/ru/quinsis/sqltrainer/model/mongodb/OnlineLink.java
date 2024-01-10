package ru.quinsis.sqltrainer.model.mongodb;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "online links")
@Getter
@Setter
public class OnlineLink {
    @Id
    private String id;

    @Field(name = "schema_id")
    private String schemaId;

    @Field(name = "code")
    private String code;
}
