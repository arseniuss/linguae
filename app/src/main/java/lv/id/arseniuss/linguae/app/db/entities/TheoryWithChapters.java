package lv.id.arseniuss.linguae.app.db.entities;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.List;

public class TheoryWithChapters {
    @Embedded
    public TheoryEntity Theory = new TheoryEntity();

    @Relation(parentColumn = "theory_id", entityColumn = "chapter_id",
            associateBy = @Junction(TheoryChapterCrossref.class))
    public List<ChapterEntity> Chapters = new ArrayList<>();
}
