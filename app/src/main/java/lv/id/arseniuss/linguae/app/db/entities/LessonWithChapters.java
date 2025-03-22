package lv.id.arseniuss.linguae.app.db.entities;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.List;

public class LessonWithChapters {
    @Embedded
    public LessonEntity Lesson = new LessonEntity();

    @Relation(parentColumn = "lesson_id", entityColumn = "chapter_id",
            associateBy = @Junction(LessonChapterCrossref.class))
    public List<ChapterEntity> Chapters = new ArrayList<>();
}
