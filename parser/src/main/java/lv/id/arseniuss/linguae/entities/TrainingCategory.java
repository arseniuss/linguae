package lv.id.arseniuss.linguae.entities;

import lv.id.arseniuss.linguae.enumerators.TaskType;

public class TrainingCategory {
    public int Id;
    public String TrainingId = "";
    public TaskType Task = TaskType.UnknownTask;
    public String Category = "";
    public String Description = "";
}
