package lv.id.arseniuss.linguae.db.tasks;

import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.entities.Task;

public class ChooseTask extends Task.ITaskData {

    public String Description;

    public String Word;

    public String Answer;

    public String Additionals;

    public ChooseTask() {
        super(TaskType.ChooseTask);
    }
}
