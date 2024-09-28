package lv.id.arseniuss.linguae.db.dataaccess;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lv.id.arseniuss.linguae.db.entities.Config;
import lv.id.arseniuss.linguae.db.entities.Setting;
import lv.id.arseniuss.linguae.db.entities.Task;

@Dao
public abstract class TaskDataAccess {
    @Query("SELECT task_id FROM lesson_task WHERE lesson_id = :lessonId")
    protected abstract List<String> GetLessonTasks(String lessonId);

    @Query("SELECT task_id FROM training_task WHERE training_id = :trainingId OR :trainingId = ''")
    protected abstract List<String> GetTrainingTasks(String trainingId);

    @Query("SELECT COUNT(*) FROM lesson_task WHERE lesson_id = :lessonId")
    protected abstract int GetLessonTaskCount(String lessonId);

    @Query("SELECT COUNT(*) FROM training_task WHERE training_id = :trainingId OR :trainingId = ''")
    protected abstract int GetTrainingTaskCount(String trainingId);

    @Query("SELECT * FROM task WHERE id IN (:taskIds)")
    protected abstract List<Task> GetTasks(List<String> taskIds);

    @Query("SELECT * FROM setting WHERE key = :setting LIMIT 1")
    public abstract Maybe<Setting> GetSetting(String setting);

    @Query("SELECT * from config")
    public abstract Single<List<Config>> GetTaskConfig();

    public Single<List<Task>> SelectLessonTasks(String lessonId, int count) {
        return Single.fromCallable(() -> {
            int taskCount = GetLessonTaskCount(lessonId);

            if (taskCount < count) {
                throw new RuntimeException(
                        "Not enough tasks in the lesson (got: " + taskCount + ", needs: " + count + ")");
            }

            int[] indexes = new Random().ints(0, taskCount).distinct().limit(count).toArray();
            List<String> taskIds = GetLessonTasks(lessonId);

            List<String> selectedIds = Arrays.stream(indexes).mapToObj(taskIds::get).collect(Collectors.toList());

            return GetTasks(selectedIds);
        });
    }

    public Single<List<Task>> SelectTrainingTasks(String trainingId, int count) {
        return Single.fromCallable(() -> {
            int taskCount = GetTrainingTaskCount(trainingId);

            if (taskCount < count) {
                throw new RuntimeException(
                        "Not enough tasks in the training (got: " + taskCount + ", needs: " + count + ")");
            }

            int[] indexes = new Random().ints(0, taskCount).distinct().limit(count).toArray();
            List<String> taskIds = GetTrainingTasks(trainingId);
            List<String> selectedIds = Arrays.stream(indexes).mapToObj(taskIds::get).collect(Collectors.toList());

            return GetTasks(selectedIds);
        });
    }
}
