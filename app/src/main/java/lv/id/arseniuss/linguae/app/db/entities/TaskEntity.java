package lv.id.arseniuss.linguae.app.db.entities;

import static lv.id.arseniuss.linguae.tasks.Task.deserialize;

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

import java.util.Objects;

import lv.id.arseniuss.linguae.tasks.Task;
import lv.id.arseniuss.linguae.types.TaskType;

@Entity(tableName = "task") public class TaskEntity {
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
    @TypeConverters({TaskEntity.class})
    public Task.ITaskData Data;

    public TaskEntity() {
    }

    public TaskEntity(@NonNull String id) {
        Id = id;
    }

    public TaskEntity(Task t) {
        Id = t.Id;
        Type = t.Type;
        Category = t.Category;
        Description = t.Description;
        Amount = t.Amount;
        Data = t.Data;
    }

    @TypeConverter
    public static String TaskToString(Task.ITaskData task) {
        Gson gson = new Gson();

        if (Objects.requireNonNull(task.Type) == TaskType.SelectTask ||
                task.Type == TaskType.TranslateTask || task.Type == TaskType.DeclineTask ||
                task.Type == TaskType.ConjugateTask || task.Type == TaskType.ChooseTask) {
            return gson.toJson(task);
        }
        return null;
    }

    @TypeConverter
    public static Task.ITaskData StringToTask(String str) {
        JsonElement jelement = JsonParser.parseString(str);
        JsonObject jobject = jelement.getAsJsonObject();

        return deserialize(jobject);
    }


    public static Gson GetGson() {
        return new GsonBuilder().registerTypeAdapter(Task.ITaskData.class,
                new TaskDataTypeAdapter()).create();
    }

    public static class TaskDataTypeAdapter implements JsonDeserializer<Task.ITaskData> {

        @Override
        public Task.ITaskData deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                                          JsonDeserializationContext context) throws
                JsonParseException {
            return Task.deserialize(json.getAsJsonObject());
        }
    }


}
