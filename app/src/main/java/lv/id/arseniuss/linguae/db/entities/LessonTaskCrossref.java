package lv.id.arseniuss.linguae.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "lesson_task", primaryKeys = {"lesson_id", "task_id"})
public class LessonTaskCrossref {
    @NonNull
    @ColumnInfo(name = "lesson_id")
    public String LessonId = "";

    @ColumnInfo(name = "task_id")
    @NonNull
    public String TaskId = "";

    public LessonTaskCrossref() {
    }

    public LessonTaskCrossref(@NonNull String lessonId, @NonNull String taskId) {
        LessonId = lessonId;
        TaskId = taskId;
    }
}
