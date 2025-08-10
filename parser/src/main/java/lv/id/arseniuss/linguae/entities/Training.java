package lv.id.arseniuss.linguae.entities;

import java.util.ArrayList;
import java.util.List;

import lv.id.arseniuss.linguae.tasks.Task;

public class Training {
    public String Id = "";

    public int Index;

    public String Section = "";

    public String Name = "";

    public String Description = "";

    public List<Task> Tasks = new ArrayList<>();
}
