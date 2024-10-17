package lv.id.arseniuss.linguae.db.dataaccess;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Embedded;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import lv.id.arseniuss.linguae.db.entities.Training;

@Dao
public abstract class TrainingDataAccess {
    @Query("SELECT t.*, COUNT(tt.training_id) AS task_count FROM training t LEFT JOIN training_task tt ON t.id = tt" +
           ".training_id OR t.id IS NULL OR t.id = '' GROUP by t.id")
    public abstract Single<List<TrainingWithCount>> GetTrainings();

    public static class TrainingWithCount {
        @Embedded
        public Training Training;

        @ColumnInfo(name = "task_count")
        public int TaskCount;
    }
}
