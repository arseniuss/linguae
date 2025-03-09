package lv.id.arseniuss.linguae.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.tasks.ChooseTask;
import lv.id.arseniuss.linguae.db.tasks.ConjugateTask;
import lv.id.arseniuss.linguae.db.tasks.DeclineTask;
import lv.id.arseniuss.linguae.db.tasks.SelectTask;
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
    @TypeConverters({Task.class})
    public ITaskData Data;

    public Task() {
    }

    public Task(@NonNull String id) {
        Id = id;
    }

    @TypeConverter
    public static String TaskToString(ITaskData task) {
        Gson gson = new Gson();

        if (Objects.requireNonNull(task.Type) == TaskType.SelectTask ||
                task.Type == TaskType.TranslateTask ||
                task.Type == TaskType.DeclineTask || task.Type == TaskType.ConjugateTask ||
                task.Type == TaskType.ChooseTask) {
            return gson.toJson(task);
        }
        return null;
    }

    @TypeConverter
    public static ITaskData StringToTask(String str) {
        JsonElement jelement = JsonParser.parseString(str);
        JsonObject jobject = jelement.getAsJsonObject();

        return deserialize(jobject);
    }

    private static ITaskData deserialize(JsonObject jsonObject) {
        String str = jsonObject.toString();

        switch (TaskType.ValueOf(jsonObject.get("type").getAsString())) {
            case SelectTask:
                return new Gson().fromJson(str, SelectTask.class);
            case ChooseTask:
                return new Gson().fromJson(str, ChooseTask.class);
            case ConjugateTask:
                return new Gson().fromJson(str, ConjugateTask.class);
            case TranslateTask:
                return new Gson().fromJson(str, TranslateTask.class);
            case DeclineTask:
                return new Gson().fromJson(str, DeclineTask.class);
            default:
                return null;
        }
    }

    public static Gson GetGson() {
        return new GsonBuilder()
                .registerTypeAdapter(ITaskData.class, new TaskDataTypeAdapter())
                .create();
    }

    public static class TaskDataTypeAdapter implements JsonDeserializer<ITaskData> {

        @Override
        public ITaskData deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                                     JsonDeserializationContext context) throws JsonParseException {
            return Task.deserialize(json.getAsJsonObject());
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
