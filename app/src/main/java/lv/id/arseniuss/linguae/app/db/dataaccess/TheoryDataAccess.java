package lv.id.arseniuss.linguae.app.db.dataaccess;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Embedded;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import lv.id.arseniuss.linguae.app.db.entities.ChapterEntity;
import lv.id.arseniuss.linguae.app.db.entities.TheoryEntity;

@Dao
public abstract class TheoryDataAccess {
    @Query("SELECT t.*, COUNT(tc.chapter_id) AS chapter_count FROM theory t LEFT JOIN theory_chapter tc ON t.id = tc" +
            ".theory_id  GROUP BY t.id")
    public abstract Single<List<TheoryWithCount>> GetTheories();

    @Query("SELECT c.* FROM chapter c INNER JOIN lesson_chapter lc ON lc.chapter_id = c.id WHERE lc.lesson_id = :lessonId")
    public abstract Single<List<ChapterEntity>> GetLessonChapters(String lessonId);

    @Query("SELECT c.* FROM chapter c INNER JOIN theory_chapter tc ON tc.chapter_id = c.id WHERE tc.theory_id = " +
            ":theoryId")
    public abstract Single<List<ChapterEntity>> GetTheoryChapters(String theoryId);

    public static class TheoryWithCount {
        @Embedded
        public TheoryEntity Theory;

        @ColumnInfo(name = "chapter_count")
        public int ChapterCount;
    }
}
