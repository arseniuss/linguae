package lv.id.arseniuss.linguae.tasks.entities;

import java.util.ArrayList;
import java.util.List;

import lv.id.arseniuss.linguae.db.entities.Task;
import lv.id.arseniuss.linguae.db.entities.TaskConfig;
import lv.id.arseniuss.linguae.db.entities.TaskResult;

public class SessionTaskData {
    public Task Task;

    public TaskResult Result;

    public List<TaskConfig> TaskConfig;

    public SessionTaskData(Task task) {
        this.Task = task;
        this.Result = new TaskResult(task);
        this.TaskConfig = new ArrayList<>();
    }
}
