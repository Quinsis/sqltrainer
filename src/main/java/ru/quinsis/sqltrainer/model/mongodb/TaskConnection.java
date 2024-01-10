package ru.quinsis.sqltrainer.model.mongodb;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter
@Setter
@Builder
public class TaskConnection {
    @Field("user_id")
    private Long userId;

    @Field("attempts")
    private List<TaskAttempt> attempts;

    @Field("is_completed")
    private boolean isCompleted;
}
