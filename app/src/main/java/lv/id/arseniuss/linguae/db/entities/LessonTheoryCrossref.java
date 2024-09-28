package lv.id.arseniuss.linguae.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "lesson_theory", primaryKeys = { "lesson_id", "theory_id" })
public class LessonTheoryCrossref {
    @NonNull
    @ColumnInfo(name = "lesson_id")
    public String LessonId = "";

    @NonNull
    @ColumnInfo(name = "theory_id")
    public String TheoryId = "";

    public LessonTheoryCrossref() {

    }

    public LessonTheoryCrossref(@NonNull String lessonId, @NonNull String theoryId) {
        LessonId = lessonId;
        TheoryId = theoryId;
    }
}
