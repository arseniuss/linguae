package lv.id.arseniuss.linguae.app.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "theory_chapter", primaryKeys = {"theory_id", "chapter_id"})
public class TheoryChapterCrossref {
    @ColumnInfo(name = "theory_id")
    @NonNull
    public String TheoryId = "";

    @ColumnInfo(name = "chapter_id")
    @NonNull
    public String ChapterId = "";

    public TheoryChapterCrossref() {
    }

    public TheoryChapterCrossref(@NonNull String theoryId, @NonNull String chapterId) {
        TheoryId = theoryId;
        ChapterId = chapterId;
    }
}
