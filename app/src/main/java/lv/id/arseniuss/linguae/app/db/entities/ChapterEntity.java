package lv.id.arseniuss.linguae.app.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lv.id.arseniuss.linguae.entities.Chapter;

@Entity(tableName = "chapter")
public class ChapterEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public String Id = "";

    @ColumnInfo(name = "text")
    public String Text;

    public ChapterEntity() {

    }

    public ChapterEntity(Chapter c) {
        Id = c.Id;
        Text = c.Text;
    }
}
