package lv.id.arseniuss.linguae.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "theory")
public class Theory {
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
}
