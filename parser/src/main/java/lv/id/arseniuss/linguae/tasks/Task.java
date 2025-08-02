package lv.id.arseniuss.linguae.tasks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import lv.id.arseniuss.linguae.enumerators.TaskType;

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


    public abstract static class ITaskData {
        @SerializedName("type")
        public TaskType Type;

        public ITaskData(TaskType type) {
            this.Type = type;
        }

        public abstract String GetTitle();
    }
}
