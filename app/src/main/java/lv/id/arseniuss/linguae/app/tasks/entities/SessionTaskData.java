package lv.id.arseniuss.linguae.app.tasks.entities;

import lv.id.arseniuss.linguae.app.db.entities.TaskEntity;
import lv.id.arseniuss.linguae.app.db.entities.TaskResultEntity;

public class SessionTaskData {
    public TaskEntity Task;

    public TaskResultEntity Result;

    public SessionTaskData(TaskEntity task) {
        this.Task = task;
        this.Result = new TaskResultEntity(task);
    }
}
