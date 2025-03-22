package lv.id.arseniuss.linguae.app.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "lesson_chapter", primaryKeys = {"lesson_id", "chapter_id"})
public class LessonChapterCrossref {
    @ColumnInfo(name = "lesson_id")
    @NonNull
    public String LessonId = "";

    @ColumnInfo(name = "chapter_id")
    @NonNull
    public String ChapterId = "";

    public LessonChapterCrossref() {
    }

    public LessonChapterCrossref(String lessonId, String chapterId) {
        LessonId = lessonId;
        ChapterId = chapterId;
    }
}
