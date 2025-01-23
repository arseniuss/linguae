package lv.id.arseniuss.linguae.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "training")
public class Training {
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
}
