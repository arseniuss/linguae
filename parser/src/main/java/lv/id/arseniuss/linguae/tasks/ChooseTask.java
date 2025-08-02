package lv.id.arseniuss.linguae.tasks;

import lv.id.arseniuss.linguae.enumerators.TaskType;

public class ChooseTask extends Task.ITaskData {

    public String Description;

    public String Word;

    public String Answer;

    public String Additionals;

    public ChooseTask() {
        super(TaskType.ChooseTask);
    }

    @Override
    public String GetTitle() {
        return Word;
    }
}
