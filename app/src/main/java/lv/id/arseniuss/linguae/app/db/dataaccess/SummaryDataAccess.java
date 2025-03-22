package lv.id.arseniuss.linguae.app.db.dataaccess;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public abstract class SummaryDataAccess {

    @Query("SELECT l.name, SUM(tr.points) AS points, SUM(tr.amount) AS amount " +
            " FROM task_result tr " +
            "INNER JOIN lesson_task lt ON lt.task_id = tr.task_id " +
            "INNER JOIN lesson l ON l.id = lt.lesson_id GROUP BY l.id ORDER BY points DESC LIMIT 3")
    public abstract Single<List<BestLesson>> GetBestLessons();

    public static class BestLesson {
        @ColumnInfo(name = "name")
        public String Name;

        @ColumnInfo(name = "points")
        public Integer Points;

        @ColumnInfo(name = "amount")
        public Integer Amount;
    }
}
