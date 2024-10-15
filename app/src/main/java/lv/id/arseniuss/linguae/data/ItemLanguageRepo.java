package lv.id.arseniuss.linguae.data;

public class ItemLanguageRepo {
    public String Name;
    public String Location;

    public ItemLanguageRepo() {

    }

    public ItemLanguageRepo(String location) {
        this.Location = location;
    }

    public ItemLanguageRepo(String name, String location) {
        this.Name = name;
        this.Location = location;
    }

}
