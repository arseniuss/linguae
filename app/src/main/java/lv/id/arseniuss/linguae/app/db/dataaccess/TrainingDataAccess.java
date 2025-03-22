package lv.id.arseniuss.linguae.app.db.dataaccess;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Embedded;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import lv.id.arseniuss.linguae.app.db.entities.TrainingCategoryEntity;
import lv.id.arseniuss.linguae.app.db.entities.TrainingEntity;

@Dao
public abstract class TrainingDataAccess {
    @Query("SELECT t.*, COUNT(tt.training_id) AS task_count, COUNT(tc.training_id) AS category_count " +
            "FROM training t " +
            "LEFT JOIN training_task tt ON t.id = tt.training_id OR t.id IS NULL OR t.id = '' " +
            "LEFT JOIN training_category tc ON t.id = tc.training_id OR t.id IS NULL OR t.id = '' " +
            "GROUP by t.id " +
            "ORDER BY t.`index`")
    public abstract Single<List<TrainingWithCount>> GetTrainings();

    @Query("SELECT * FROM training_category WHERE training_id = :trainingId OR :trainingId IS NULL OR :trainingId = ''")
    public abstract Single<List<TrainingCategoryEntity>> GetCategories(String trainingId);

    public static class TrainingWithCount {
        @Embedded
        public TrainingEntity Training;

        @ColumnInfo(name = "task_count")
        public int TaskCount;

        @ColumnInfo(name = "category_count")
        public int CategoryCount;
    }
}
