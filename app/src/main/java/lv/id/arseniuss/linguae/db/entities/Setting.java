package lv.id.arseniuss.linguae.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lv.id.arseniuss.linguae.data.SettingType;

@Entity(tableName = "setting")
public class Setting {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "key")
    public String Key = "";

    @NonNull
    @ColumnInfo(name = "description")
    public String Description = "";

    @NonNull
    @ColumnInfo(name = "type")
    public SettingType Type;

    @ColumnInfo(name = "value")
    public String Value;

    public Setting() { }

    public Setting(@NonNull String key, String value) {
        Key = key;
        Value = value;
    }
}
