package lv.id.arseniuss.linguae.app.db.entities;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.nio.charset.StandardCharsets;

import lv.id.arseniuss.linguae.app.Utilities;
import lv.id.arseniuss.linguae.enumerators.TaskType;
import lv.id.arseniuss.linguae.tasks.ChooseTask;
import lv.id.arseniuss.linguae.tasks.ConjugateTask;
import lv.id.arseniuss.linguae.tasks.DeclineTask;
import lv.id.arseniuss.linguae.tasks.SelectTask;
import lv.id.arseniuss.linguae.tasks.Task;
import lv.id.arseniuss.linguae.tasks.TranslateTask;

@Entity(tableName = "task")
public class TaskEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @SerializedName("id")
    @NonNull
    public String Id = "";

    @ColumnInfo(name = "type")
    @SerializedName("type")
    @NonNull
    public TaskType Type = TaskType.UnknownTask;

    /* category of noun (declination) or verb (conjugation) */
    @ColumnInfo(name = "category")
    @SerializedName("category")
    public String Category;

    @ColumnInfo(name = "description")
    @SerializedName("description")
    public String Description;

    @ColumnInfo(name = "amount")
    @SerializedName("amount")
    public long Amount;

    @ColumnInfo(name = "data")
    @SerializedName("data")
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
        if (task == null) return null;

        String json = Utilities.GetGson().toJson(task);

        return Base64.encodeToString(json.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
    }

    @TypeConverter
    public static Task.ITaskData StringToTask(String str) {
        if (str == null) return null;

        byte[] decodedBytes = Base64.decode(str, Base64.NO_WRAP);
        String json = new String(decodedBytes, StandardCharsets.UTF_8);

        JsonElement jelement = JsonParser.parseString(json);
        JsonObject jobject = jelement.getAsJsonObject();

        return deserialize(jobject);
    }

    public static Task.ITaskData deserialize(JsonObject jsonObject) {
        String str = jsonObject.toString();

        switch (TaskType.FromValue(jsonObject.get("type").getAsInt())) {
            case SelectTask:
                return Utilities.GetGson().fromJson(str, SelectTask.class);
            case ChooseTask:
                return Utilities.GetGson().fromJson(str, ChooseTask.class);
            case ConjugateTask:
                return Utilities.GetGson().fromJson(str, ConjugateTask.class);
            case TranslateTask:
                return Utilities.GetGson().fromJson(str, TranslateTask.class);
            case DeclineTask:
                return Utilities.GetGson().fromJson(str, DeclineTask.class);
            default:
                return null;
        }
    }
}
