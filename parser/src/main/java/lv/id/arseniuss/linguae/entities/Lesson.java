package lv.id.arseniuss.linguae.entities;

import java.util.ArrayList;
import java.util.List;

import lv.id.arseniuss.linguae.tasks.Task;

public class Lesson {
    public String Id;
    public int Index;
    public String Name = "";
    public String Description = "";

    public List<Task> Tasks = new ArrayList<>();
    public List<Theory> Theories = new ArrayList<>();
    public List<Chapter> Chapters = new ArrayList<>();
}