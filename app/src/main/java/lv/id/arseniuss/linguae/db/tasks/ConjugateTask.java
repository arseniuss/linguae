package lv.id.arseniuss.linguae.db.tasks;

import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.entities.Task;

public class ConjugateTask extends Task.ITaskData {
    public String Verb;

    public String Meaning;

    public String[] Persons;

    public String[] Answers;

    public ConjugateTask() {
        super(TaskType.ConjugateTask);
    }

    @Override
    public String GetTitle() {
        return Verb;
    }
}
