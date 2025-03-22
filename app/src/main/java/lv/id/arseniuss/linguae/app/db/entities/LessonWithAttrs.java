package lv.id.arseniuss.linguae.app.db.entities;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.List;

public class LessonWithAttrs {
    @Embedded
    public LessonEntity Lesson = new LessonEntity();

    @Relation(parentColumn = "lesson_id", entityColumn = "task_id",
            associateBy = @Junction(LessonTaskCrossref.class))
    public List<TaskEntity> Tasks = new ArrayList<>();

    @Relation(parentColumn = "lesson_id", entityColumn = "theory_id",
            associateBy = @Junction(LessonTheoryCrossref.class))
    public List<TheoryEntity> Theories = new ArrayList<>();

    @Relation(parentColumn = "lesson_id", entityColumn = "chapter_id",
            associateBy = @Junction(LessonChapterCrossref.class))
    public List<ChapterEntity> Chapters = new ArrayList<>();
}
