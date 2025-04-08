package lv.id.arseniuss.linguae.entities;

import java.util.ArrayList;
import java.util.List;

import lv.id.arseniuss.linguae.parsers.LanguageDataParser;

public class Repository {
    public String Name = "";
    public String Location = "";
    public List<Language> Languages = new ArrayList<>();
    public String Error = "";

    public Repository() {

    }

    public Repository(String name, String location) {
        Name = name;
        Location = location;
    }
}
