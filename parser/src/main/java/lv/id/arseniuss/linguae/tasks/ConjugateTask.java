package lv.id.arseniuss.linguae.tasks;

import lv.id.arseniuss.linguae.enumerators.TaskType;

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
