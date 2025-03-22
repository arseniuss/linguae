package lv.id.arseniuss.linguae.app.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lv.id.arseniuss.linguae.entities.License;

@Entity(tableName = "license") public class LicenseEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public String Id = "";

    @ColumnInfo(name = "text")
    @NonNull
    public String Text = "";

    public LicenseEntity() {

    }

    public LicenseEntity(License l) {
        Id = l.Id;
        Text = l.Text;
    }
}
