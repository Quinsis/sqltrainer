package ru.quinsis.sqltrainer.model.mongodb;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Table {
    @Field(name = "table_name")
    private String name;

    @Field(name = "columns")
    private List<Column> columns;

    @Field(name = "values")
    private List<Map<String, Object>> values;
}
