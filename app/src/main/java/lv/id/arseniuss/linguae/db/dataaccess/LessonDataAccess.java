package lv.id.arseniuss.linguae.db.dataaccess;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Embedded;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import lv.id.arseniuss.linguae.db.entities.Lesson;

@Dao
public abstract class LessonDataAccess {
    @Query("SELECT l.*, COALESCE(lt1.done_count, 0) as done_count, COALESCE(t.task_count, 0) AS task_count,          " +
           "COALESCE(th.theory_count, 0) AS theory_count FROM lesson l " +
           "LEFT JOIN (SELECT lt.lesson_id, COUNT(*) as done_count FROM task_result tr" +
           "           INNER JOIN lesson_task lt ON lt.task_id = tr.task_id" +
           "           WHERE tr.points = tr.amount                                                                   " +
           "           GROUP BY lt.lesson_id                                                                         " +
           ") lt1 ON l.id = lt1.lesson_id " +
           "LEFT JOIN (SELECT lesson_id, COUNT(*) AS task_count FROM lesson_task GROUP BY lesson_id) t " +
           "ON l.id = t.lesson_id " +
           "LEFT JOIN ( SELECT lesson_id, COUNT(*) AS theory_count FROM lesson_theory GROUP BY lesson_id) th ON l.id " +
           "= th.lesson_id ORDER BY l.id")
    public abstract Single<List<LessonWithCount>> GetLessons();

    public static class LessonWithCount {
        @Embedded
        public Lesson Lesson;

        @ColumnInfo(name = "done_count")
        public int DoneCount;

        @ColumnInfo(name = "task_count")
        public int TaskCount;

        @ColumnInfo(name = "theory_count")
        public int TheoryCount;
    }
}
