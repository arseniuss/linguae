package lv.id.arseniuss.linguae.app.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import lv.id.arseniuss.linguae.app.db.enumerators.CheckpointType;

@Entity(tableName = "checkpoint", primaryKeys = {"type", "id"})
public class CheckpointEntity {
    @ColumnInfo(name = "type")
    @NonNull
    public CheckpointType Type = CheckpointType.UnknownCheckpoint;

    @NonNull
    @ColumnInfo(name = "id")
    public String Id = "";
}
