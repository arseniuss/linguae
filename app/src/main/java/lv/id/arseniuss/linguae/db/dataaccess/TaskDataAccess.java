package lv.id.arseniuss.linguae.db.dataaccess;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.entities.Setting;
import lv.id.arseniuss.linguae.db.entities.Task;

@Dao
public abstract class TaskDataAccess {
    @Query("SELECT task_id FROM lesson_task WHERE lesson_id = :lessonId")
    protected abstract List<String> GetLessonTasks(String lessonId);

    @Query("SELECT t.* FROM task t INNER JOIN lesson_task lt on lt.task_id = t.id WHERE lt.lesson_id = :lessonId")
    public abstract Single<List<Task>> GetTasksByLesson(String lessonId);

    @Query("SELECT task_id FROM training_task WHERE training_id = :trainingId OR :trainingId = ''")
    protected abstract List<String> GetTrainingTasks(String trainingId);

    @Query("SELECT tt.task_id FROM training_task tt INNER JOIN task t ON t.id = tt.task_id WHERE (tt.training_id = " +
            ":trainingId OR :trainingId IS NULL OR :trainingId = '') AND t.type = :taskType AND t.category = :category" +
            " AND t.description = :description")
    protected abstract List<String> GetTrainingTasks(String trainingId, TaskType taskType,
                                                     String category,
                                                     String description);

    @Query("SELECT COUNT(*) FROM lesson_task WHERE lesson_id = :lessonId")
    protected abstract int GetLessonTaskCount(String lessonId);

    @Query("SELECT COUNT(*) FROM training_task WHERE training_id = :trainingId OR :trainingId = ''")
    protected abstract int GetTrainingTaskCount(String trainingId);

    @Query("SELECT * FROM task WHERE id IN (:taskIds)")
    protected abstract List<Task> GetTasks(List<String> taskIds);

    @Query("SELECT * FROM setting WHERE `key` = :setting LIMIT 1")
    public abstract Maybe<Setting> GetSetting(String setting);

    public Single<List<Task>> SelectLessonTasks(String lessonId, int count) {
        return Single.fromCallable(() -> {
            int taskCount = GetLessonTaskCount(lessonId);

            if (taskCount < count) {
                throw new RuntimeException(
                        "Not enough tasks in the lesson (got: " + taskCount + ", needs: " + count +
                                ")");
            }

            int[] indexes = new Random().ints(0, taskCount).distinct().limit(count).toArray();
            List<String> taskIds = GetLessonTasks(lessonId);

            List<String> selectedIds =
                    Arrays.stream(indexes).mapToObj(taskIds::get).collect(Collectors.toList());

            return GetTasks(selectedIds);
        });
    }

    public Single<List<Task>> SelectTrainingTasks(String trainingId, int count,
                                                  List<TrainingCategory> categories) {
        return Single.fromCallable(() -> {
            List<String> taskIds;

            if (categories.isEmpty()) {
                taskIds = GetTrainingTasks(trainingId);
            } else {
                taskIds = new ArrayList<>();

                for (TrainingCategory tc : categories) {
                    taskIds.addAll(
                            GetTrainingTasks(trainingId, tc.TaskType, tc.Category, tc.Description));
                }
            }

            if (taskIds.size() < count) {
                throw new RuntimeException(
                        "Not enough tasks in the training (got: " + taskIds.size() + ", needs: " +
                                count + ")");
            }

            int[] indexes = new Random().ints(0, taskIds.size()).distinct().limit(count).toArray();

            List<String> selectedIds =
                    Arrays.stream(indexes).mapToObj(taskIds::get).collect(Collectors.toList());

            return GetTasks(selectedIds);
        });
    }

    public static class TrainingCategory {
        public TaskType TaskType;
        public String Category;
        public String Description;

        public TrainingCategory() {

        }

        public TrainingCategory(TaskType taskType, String category, String description) {
            this.TaskType = taskType;
            Category = category;
            Description = description;
        }
    }
}
