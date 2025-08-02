package lv.id.arseniuss.linguae.app.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

import lv.id.arseniuss.linguae.enumerators.TaskType;

@Entity(tableName = "task_result") public class TaskResultEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long Id;

    @ColumnInfo(name = "session_id")
    public long SessionId;

    @ColumnInfo(name = "task_id")
    @NonNull
    public String TaskId = "";

    @ColumnInfo(name = "task_type")
    @NonNull
    public TaskType Type = TaskType.UnknownTask;

    @ColumnInfo(name = "points")
    public int Points = 0;

    @ColumnInfo(name = "amount")
    public int Amount = 1;

    @Ignore
    public List<TaskError> Errors = new ArrayList<>();

    public TaskResultEntity() {
    }

    public TaskResultEntity(@NonNull TaskType taskType, int points, int amount) {
        this.Type = taskType;
        this.Points = points;
        this.Amount = amount;
    }

    public TaskResultEntity(TaskEntity task) {
        this.Type = task.Type;
        this.TaskId = task.Id;
    }
}
