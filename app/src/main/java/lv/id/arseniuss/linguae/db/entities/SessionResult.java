package lv.id.arseniuss.linguae.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "session_result")
public class SessionResult {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long Id;

    @ColumnInfo(name = "start")
    @NonNull
    public Date StartTime = new Date();

    @ColumnInfo(name = "time")
    public int PassedTime;

    @ColumnInfo(name = "points")
    public int Points;

    @ColumnInfo(name = "amount")
    public int Amount;
}
