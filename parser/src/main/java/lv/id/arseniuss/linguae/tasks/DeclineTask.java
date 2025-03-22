package lv.id.arseniuss.linguae.tasks;

import lv.id.arseniuss.linguae.types.TaskType;

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
