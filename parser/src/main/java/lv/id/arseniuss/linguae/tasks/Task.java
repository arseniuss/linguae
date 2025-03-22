package lv.id.arseniuss.linguae.tasks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import lv.id.arseniuss.linguae.types.TaskType;

public class Task {
    public String Id = "";

    public TaskType Type = TaskType.UnknownTask;

    public String Category;

    public String Description;

    public long Amount;

    public ITaskData Data;

    public Task() {

    }

    public Task(String id) {
        Id = id;
    }

    public static ITaskData deserialize(JsonObject jsonObject) {
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

    public abstract static class ITaskData {
        @SerializedName("type")
        public TaskType Type;

        public ITaskData(TaskType type) {
            this.Type = type;
        }

        public abstract String GetTitle();
    }
}
