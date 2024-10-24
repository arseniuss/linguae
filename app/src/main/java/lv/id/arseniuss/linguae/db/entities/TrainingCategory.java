package lv.id.arseniuss.linguae.db.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

import lv.id.arseniuss.linguae.data.TaskType;

@Entity(tableName = "training_category", indices = { @Index(value = "training_id") },
        foreignKeys = @ForeignKey(entity = Training.class, parentColumns = "id", childColumns = "training_id",
                onDelete = ForeignKey.CASCADE))
public class TrainingCategory {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int Id;

    @ColumnInfo(name = "training_id")
    @NonNull
    public String TrainingId = "";

    @ColumnInfo(name = "type")
    @NonNull
    public TaskType Task = TaskType.UnknownTask;

    @ColumnInfo(name = "category")
    @NonNull
    public String Category = "";

    @ColumnInfo(name = "description")
    @NonNull
    public String Description = "";

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrainingCategory tc = (TrainingCategory) o;

        return Objects.equals(this.Task, tc.Task) && Objects.equals(this.Category, tc.Category) &&
               Objects.equals(this.Description, tc.Description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.Task, this.Category, this.Description);
    }
}
