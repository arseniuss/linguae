package lv.id.arseniuss.linguae.app.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "config")
public class ConfigEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "key")
    public String Key = "";

    @ColumnInfo(name = "value")
    public String Value;

    public ConfigEntity() {
    }

    public ConfigEntity(@NonNull String key, String value) {
        Key = key;
        Value = value;
    }

    public String GetKey() {
        return Key;
    }

    public String GetValue() {
        return Value;
    }
}
