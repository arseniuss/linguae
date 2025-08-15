package lv.id.arseniuss.linguae.entities;

import java.util.ArrayList;
import java.util.List;

public class Theory {
    public String Id = "";
    public int Index;
    public String Title = "";
    public String Description = "";
    public String Section = "";

    public List<Chapter> Chapters = new ArrayList<>();
}
