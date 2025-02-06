package lv.id.arseniuss.linguae.db.tasks;

import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.entities.Task;

public class SelectTask extends Task.ITaskData {
    public String Meaning;

    public String[] Sentence;

    public String[] Answers;

    public String[] Options;

    public SelectTask() {
        super(TaskType.SelectTask);
    }

    @Override
    public String GetTitle() {
        return Meaning;
    }
}
