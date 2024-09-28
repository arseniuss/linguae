package lv.id.arseniuss.linguae.db.tasks;

import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.entities.Task;

public class ChooseTask extends Task.ITaskData {

    public String Word;

    public String Meaning;

    public String Answer;

    public String Options;

    public String Description;

    public ChooseTask() {
        super(TaskType.ChooseTask);
    }
}
