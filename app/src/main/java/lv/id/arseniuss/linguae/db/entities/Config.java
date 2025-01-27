package lv.id.arseniuss.linguae.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "config")
public class Config {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "key")
    public String Key = "";

    @ColumnInfo(name = "value")
    public String Value;

    public Config() {
    }

    public Config(@NonNull String key, String value) {
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
