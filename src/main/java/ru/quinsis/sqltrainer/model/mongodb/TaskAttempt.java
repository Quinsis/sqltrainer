package ru.quinsis.sqltrainer.model.mongodb;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Getter
@Setter
@Builder
public class TaskAttempt {
    public static enum Status {
        ERROR,
        ACCEPTED
    }

    @Field("query")
    private String query;

    @Field("status")
    private Status status;

    @Field("date")
    private Date date;
}
