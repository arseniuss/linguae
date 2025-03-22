package lv.id.arseniuss.linguae.app.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lv.id.arseniuss.linguae.entities.Theory;

@Entity(tableName = "theory")
public class TheoryEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public String Id = "";

    @ColumnInfo(name = "index")
    public int Index;

    @ColumnInfo(name = "title")
    public String Title = "";

    @ColumnInfo(name = "description")
    public String Description = "";

    public TheoryEntity() {

    }

    public TheoryEntity(Theory t) {
        Id = t.Id;
        Index = t.Index;
        Title = t.Title;
        Description = t.Description;
    }
}
