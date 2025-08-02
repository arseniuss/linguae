package lv.id.arseniuss.linguae.app.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lv.id.arseniuss.linguae.entities.Lesson;

@Entity(tableName = "lesson") public class LessonEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public String Id;

    @ColumnInfo(name = "index")
    public int Index;

    @ColumnInfo(name = "name")
    @NonNull
    public String Name = "";

    @ColumnInfo(name = "section")
    @NonNull
    public String Section = "";

    @ColumnInfo(name = "description")
    @NonNull
    public String Description = "";


    public LessonEntity() {

    }

    public LessonEntity(Lesson l) {
        Id = l.Id;
        Index = l.Index;
        Name = l.Name;
        Section = l.Section;
        Description = l.Description;
    }
}
