package lv.id.arseniuss.linguae.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.tasks.CasingTask;
import lv.id.arseniuss.linguae.db.tasks.ChooseTask;
import lv.id.arseniuss.linguae.db.tasks.ConjugateTask;
import lv.id.arseniuss.linguae.db.tasks.DeclineTask;
import lv.id.arseniuss.linguae.db.tasks.MacronTask;
import lv.id.arseniuss.linguae.db.tasks.NumberTask;
import lv.id.arseniuss.linguae.db.tasks.TranslateTask;

@Entity(tableName = "task")
public class Task {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public String Id = "";

    @ColumnInfo(name = "type")
    @NonNull
    public TaskType Type = TaskType.UnknownTask;

    /* category of noun (declination) or verb (conjugation) */
    @ColumnInfo(name = "category")
    public String Category;

    @ColumnInfo(name = "description")
    public String Description;

    @ColumnInfo(name = "amount")
    public long Amount;

    @ColumnInfo(name = "data")
    @TypeConverters({ Task.class })
    public ITaskData Data;

    public Task() { }

    public Task(@NonNull String id) {
        Id = id;
    }

    @TypeConverter
    public static String TaskToString(ITaskData task) {
        Gson gson = new Gson();

        if (Objects.requireNonNull(task.Type) == TaskType.CasingTask || task.Type == TaskType.TranslateTask ||
            task.Type == TaskType.MacronTask || task.Type == TaskType.NumberTask || task.Type == TaskType.DeclineTask ||
            task.Type == TaskType.ConjugateTask || task.Type == TaskType.ChooseTask)
        {
            return gson.toJson(task);
        }
        return null;
    }

    @TypeConverter
    public static ITaskData StringToTask(String str) {
        JsonElement jelement = JsonParser.parseString(str);
        JsonObject jobject = jelement.getAsJsonObject();

        switch (TaskType.ValueOf(jobject.get("type").getAsString())) {
            case CasingTask:
                return new Gson().fromJson(str, CasingTask.class);
            case ChooseTask:
                return new Gson().fromJson(str, ChooseTask.class);
            case ConjugateTask:
                return new Gson().fromJson(str, ConjugateTask.class);
            case MacronTask:
                return new Gson().fromJson(str, MacronTask.class);
            case NumberTask:
                return new Gson().fromJson(str, NumberTask.class);
            case TranslateTask:
                return new Gson().fromJson(str, TranslateTask.class);
            case DeclineTask:
                return new Gson().fromJson(str, DeclineTask.class);
            default:
                return null;
        }
    }

    public abstract static class ITaskData {
        @SerializedName("type")
        public TaskType Type;

        public abstract String GetTitle();

        public ITaskData(TaskType type) {
            this.Type = type;
        }
    }
}
