package lv.id.arseniuss.linguae.tasks;

import lv.id.arseniuss.linguae.enumerators.TaskType;

public class TranslateTask extends Task.ITaskData {
    public String Text;

    public String[] Answer;

    public String Additional;

    public TranslateTask() {
        super(TaskType.TranslateTask);
    }

    @Override
    public String GetTitle() {
        return Text;
    }
}
