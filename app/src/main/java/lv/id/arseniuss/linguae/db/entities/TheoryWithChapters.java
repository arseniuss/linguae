package lv.id.arseniuss.linguae.db.entities;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.List;

public class TheoryWithChapters {
    @Embedded
    public Theory Theory = new Theory();

    @Relation(parentColumn = "theory_id", entityColumn = "chapter_id",
            associateBy = @Junction(TheoryChapterCrossref.class))
    public List<Chapter> Chapters = new ArrayList<>();
}
