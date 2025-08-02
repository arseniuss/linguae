package lv.id.arseniuss.linguae.app.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lv.id.arseniuss.linguae.app.db.enumerators.BugLocation;

@Entity(tableName = "bug_report")
public class BugReportEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int Id;

    @ColumnInfo(name = "location")
    @NonNull
    public BugLocation Location = BugLocation.UNKNOWN_LOCATION;

    @ColumnInfo(name = "description")
    @NonNull
    public String Description = "";

    @ColumnInfo(name = "data")
    public String Data;

    @ColumnInfo(name = "screenshot")
    public String Screenshot;
}
