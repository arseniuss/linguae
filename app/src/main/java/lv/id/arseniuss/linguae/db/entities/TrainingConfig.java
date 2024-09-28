package lv.id.arseniuss.linguae.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

import lv.id.arseniuss.linguae.data.TaskType;

public class TrainingConfig {
    @ColumnInfo(name = "type")
    @NonNull
    public TaskType Type = TaskType.UnknownTask;

    @ColumnInfo(name = "category")
    public String Category;

    @ColumnInfo(name = "description")
    public String Description;

    public TaskType getType() {
        return Type;
    }
}
