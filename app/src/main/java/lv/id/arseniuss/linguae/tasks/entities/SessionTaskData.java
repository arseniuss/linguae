package lv.id.arseniuss.linguae.tasks.entities;

import lv.id.arseniuss.linguae.db.entities.Task;
import lv.id.arseniuss.linguae.db.entities.TaskResult;

public class SessionTaskData {
    public Task Task;

    public TaskResult Result;

    public SessionTaskData(Task task) {
        this.Task = task;
        this.Result = new TaskResult(task);
    }
}
