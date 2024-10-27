package lv.id.arseniuss.linguae.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.Objects;

import lv.id.arseniuss.linguae.db.dataaccess.LessonDataAccess;
import lv.id.arseniuss.linguae.db.dataaccess.LicenseDataAccess;
import lv.id.arseniuss.linguae.db.dataaccess.MainDataAccess;
import lv.id.arseniuss.linguae.db.dataaccess.SessionDataAccess;
import lv.id.arseniuss.linguae.db.dataaccess.SummaryDataAccess;
import lv.id.arseniuss.linguae.db.dataaccess.TaskDataAccess;
import lv.id.arseniuss.linguae.db.dataaccess.TheoryDataAccess;
import lv.id.arseniuss.linguae.db.dataaccess.TrainingDataAccess;
import lv.id.arseniuss.linguae.db.dataaccess.UpdateDataAccess;
import lv.id.arseniuss.linguae.db.entities.Chapter;
import lv.id.arseniuss.linguae.db.entities.Config;
import lv.id.arseniuss.linguae.db.entities.Lesson;
import lv.id.arseniuss.linguae.db.entities.LessonTaskCrossref;
import lv.id.arseniuss.linguae.db.entities.LessonTheoryCrossref;
import lv.id.arseniuss.linguae.db.entities.License;
import lv.id.arseniuss.linguae.db.entities.SessionResult;
import lv.id.arseniuss.linguae.db.entities.Setting;
import lv.id.arseniuss.linguae.db.entities.Task;
import lv.id.arseniuss.linguae.db.entities.TaskResult;
import lv.id.arseniuss.linguae.db.entities.Theory;
import lv.id.arseniuss.linguae.db.entities.TheoryChapterCrossref;
import lv.id.arseniuss.linguae.db.entities.Training;
import lv.id.arseniuss.linguae.db.entities.TrainingCategory;
import lv.id.arseniuss.linguae.db.entities.TrainingTaskCrossref;

@Database(version = 1, exportSchema = false, entities = {
        Lesson.class, LessonTaskCrossref.class, Setting.class, Task.class, Training.class, TrainingTaskCrossref.class,
        SessionResult.class, TaskResult.class, Chapter.class, Theory.class, TheoryChapterCrossref.class,
        LessonTheoryCrossref.class, Config.class, TrainingCategory.class, License.class
})
@TypeConverters({ DatabaseConverters.class })
public abstract class LanguageDatabase extends RoomDatabase {
    private static LanguageDatabase _instance;
    private String _language;

    public static synchronized LanguageDatabase GetInstance(Context context, String language) {
        if (_instance == null || !Objects.equals(_instance._language, language)) {
            _instance = Room.databaseBuilder(context.getApplicationContext(), LanguageDatabase.class, language)
                    .fallbackToDestructiveMigration()
                    .build();
            _instance._language = language;
        }
        return _instance;
    }

    public abstract SummaryDataAccess GetSummaryDataAccess();

    public abstract UpdateDataAccess GetUpdateDataAccess();

    public abstract LessonDataAccess GetLessonsDataAccess();

    public abstract TrainingDataAccess GetTrainingsDataAccess();

    public abstract TaskDataAccess GetTaskDataAccess();

    public abstract SessionDataAccess GetSessionDataAccess();

    public abstract MainDataAccess GetMainDataAccess();

    public abstract TheoryDataAccess GetTheoryDataAccess();

    public abstract LicenseDataAccess GetLicenseDataAccess();
}
