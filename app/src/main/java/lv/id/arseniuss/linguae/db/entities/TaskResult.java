package lv.id.arseniuss.linguae.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

import lv.id.arseniuss.linguae.data.TaskType;

@Entity(tableName = "task_result")
public class TaskResult {
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
    public TaskType TaskType = lv.id.arseniuss.linguae.data.TaskType.UnknownTask;

    @ColumnInfo(name = "points")
    public int Points = 0;

    @ColumnInfo(name = "amount")
    public int Amount = 1;

    @Ignore
    public List<TaskError> Errors = new ArrayList<>();

    public TaskResult() {
    }

    public TaskResult(@NonNull TaskType taskType, int points, int amount) {
        this.TaskType = taskType;
        this.Points = points;
        this.Amount = amount;
    }

    public TaskResult(Task task) {
        this.TaskType = task.Type;
        this.TaskId = task.Id;
    }
}
