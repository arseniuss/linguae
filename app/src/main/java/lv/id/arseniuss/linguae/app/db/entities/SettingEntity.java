package lv.id.arseniuss.linguae.app.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lv.id.arseniuss.linguae.entities.Setting;

@Entity(tableName = "setting") public class SettingEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "key")
    public String Key = "";

    @NonNull
    @ColumnInfo(name = "description")
    public String Description = "";

    @NonNull
    @ColumnInfo(name = "type")
    public lv.id.arseniuss.linguae.enumerators.SettingType Type;

    @ColumnInfo(name = "value")
    public String Value;

    public SettingEntity() {
    }

    public SettingEntity(@NonNull String key, String value) {
        Key = key;
        Value = value;
    }

    public SettingEntity(Setting s) {
        Key = s.Key;
        Description = s.Description;
        Type = s.Type;
        Value = s.Value;
    }
}
