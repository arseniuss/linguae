package lv.id.arseniuss.linguae.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "setting")
public class Setting {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "key")
    public String Key = "";

    @ColumnInfo(name = "value")
    public String Value;

    public Setting() { }

    public Setting(@NonNull String key, String value) {
        Key = key;
        Value = value;
    }
}
