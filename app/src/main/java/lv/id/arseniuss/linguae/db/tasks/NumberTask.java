package lv.id.arseniuss.linguae.db.tasks;

import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.entities.Task;

public class NumberTask extends Task.ITaskData {
    public String Text;

    public String Type;

    public String Answer;

    public NumberTask() {
        super(TaskType.NumberTask);
    }

    @Override
    public String GetTitle() {
        return Text;
    }
}
