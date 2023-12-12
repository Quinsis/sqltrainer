package ru.quinsis.sqltrainer.model.mongodb;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Column {
    public enum KeyStatus {
        NONE,
        PRIMARY,
        FOREIGN
    }

    @Field(name = "column_name")
    private String name;

    @Field(name = "data_type")
    private String dataType;

    @Field(name = "is_nullable")
    private boolean isNullable;

    @Field(name = "key_status")
    private KeyStatus keyStatus;

    @Field(name = "foreign_keys")
    Map<String, List<String>> foreignKeys;
}
