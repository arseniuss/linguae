package lv.id.arseniuss.linguae.app.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lv.id.arseniuss.linguae.entities.Training;

@Entity(tableName = "training")
public class TrainingEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public String Id = "";

    @ColumnInfo(name = "index")
    public int Index;

    @ColumnInfo(name = "section")
    @NonNull
    public String Section = "";

    @ColumnInfo(name = "name")
    @NonNull
    public String Name = "";

    @ColumnInfo(name = "description")
    @NonNull
    public String Description = "";

    public TrainingEntity() {

    }

    public TrainingEntity(Training t) {
        Id = t.Id;
        Index = t.Index;
        Section = t.Section;
        Name = t.Name;
        Description = t.Description;
    }
}
