package lv.id.arseniuss.linguae.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lv.id.arseniuss.linguae.tasks.Task;

public class Lesson {
    public String Id;
    public int Index;
    public String Name = "";
    public String Section = "";
    public String Description = "";

    public List<Task> Tasks = new ArrayList<>();
    public List<Theory> Theories = new ArrayList<>();
    public Map<String, Chapter> Chapters = new HashMap<>();
}