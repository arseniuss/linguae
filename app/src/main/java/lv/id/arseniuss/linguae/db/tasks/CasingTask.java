package lv.id.arseniuss.linguae.db.tasks;

import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.entities.Task;

public class CasingTask extends Task.ITaskData {
    public String Sentence;

    public String Meaning;

    public String Answers;

    public String Options;

    public CasingTask() {
        super(TaskType.CasingTask);
    }
}
