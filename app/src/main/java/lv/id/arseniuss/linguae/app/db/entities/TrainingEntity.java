package lv.id.arseniuss.linguae.app.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lv.id.arseniuss.linguae.entities.Training;

@Entity(tableName = "training") public class TrainingEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public String Id = "";

    @ColumnInfo(name = "index")
    public int Index;

    @ColumnInfo(name = "name")
    @NonNull
    public String Name = "";

    @ColumnInfo(name = "description")
    @NonNull
    public String Description = "";

    @ColumnInfo(name = "filename")
    @NonNull
    public String Filename = "";

    public TrainingEntity() {

    }

    public TrainingEntity(Training t) {
        Id = t.Id;
        Index = t.Index;
        Name = t.Name;
        Description = t.Description;
        Filename = t.Filename;
    }
}
