package lv.id.arseniuss.linguae.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import lv.id.arseniuss.linguae.data.TaskType;

@Entity(tableName = "task_config", primaryKeys = { "type", "part" })
public class TaskConfig {
    @ColumnInfo(name = "type")
    @NonNull
    public TaskType Type = TaskType.UnknownTask;

    @ColumnInfo(name = "part")
    @NonNull
    public String Part = "";

    @ColumnInfo(name = "value")
    @NonNull
    public String Value = "";

    @ColumnInfo(name = "description")
    @NonNull
    public String Description = "";
}
