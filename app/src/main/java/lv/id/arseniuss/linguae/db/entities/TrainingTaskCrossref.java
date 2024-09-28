package lv.id.arseniuss.linguae.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "training_task", primaryKeys = { "training_id", "task_id" })
public class TrainingTaskCrossref {
    @ColumnInfo(name = "training_id")
    @NonNull
    public String TrainingId = "";

    @ColumnInfo(name = "task_id")
    @NonNull
    public String TaskId = "";

    public TrainingTaskCrossref() {

    }

    public TrainingTaskCrossref(@NonNull String tid, @NonNull String tid2) {
        TrainingId = tid;
        TaskId = tid2;
    }
}
