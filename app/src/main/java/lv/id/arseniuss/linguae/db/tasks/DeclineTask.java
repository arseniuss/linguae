package lv.id.arseniuss.linguae.db.tasks;

import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.entities.Task;

public class DeclineTask extends Task.ITaskData {
    public String Word;

    public String Meaning;

    public String[] Cases;

    public String[] Answers;

    public DeclineTask() {
        super(TaskType.DeclineTask);
    }

    @Override
    public String GetTitle() {
        return Word;
    }
}
