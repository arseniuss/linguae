package lv.id.arseniuss.linguae.db.tasks;

import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.entities.Task;

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
