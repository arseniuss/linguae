package lv.id.arseniuss.linguae.db.tasks;

import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.entities.Task;

public class MacronTask extends Task.ITaskData {
    public String Text;

    public String Meaning;

    public MacronTask() {
        super(TaskType.MacronTask);
    }
}
