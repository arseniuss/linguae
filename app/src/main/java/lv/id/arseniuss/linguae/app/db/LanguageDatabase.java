package lv.id.arseniuss.linguae.app.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.Objects;

import lv.id.arseniuss.linguae.app.Configuration;
import lv.id.arseniuss.linguae.app.db.dataaccess.BugReportDataAccess;
import lv.id.arseniuss.linguae.app.db.dataaccess.CommonDataAccess;
import lv.id.arseniuss.linguae.app.db.dataaccess.LessonDataAccess;
import lv.id.arseniuss.linguae.app.db.dataaccess.LicenseDataAccess;
import lv.id.arseniuss.linguae.app.db.dataaccess.SessionDataAccess;
import lv.id.arseniuss.linguae.app.db.dataaccess.SummaryDataAccess;
import lv.id.arseniuss.linguae.app.db.dataaccess.TaskDataAccess;
import lv.id.arseniuss.linguae.app.db.dataaccess.TheoryDataAccess;
import lv.id.arseniuss.linguae.app.db.dataaccess.TrainingDataAccess;
import lv.id.arseniuss.linguae.app.db.dataaccess.UpdateDataAccess;
import lv.id.arseniuss.linguae.app.db.entities.BugReportEntity;
import lv.id.arseniuss.linguae.app.db.entities.ChapterEntity;
import lv.id.arseniuss.linguae.app.db.entities.CheckpointEntity;
import lv.id.arseniuss.linguae.app.db.entities.ConfigEntity;
import lv.id.arseniuss.linguae.app.db.entities.LessonChapterCrossref;
import lv.id.arseniuss.linguae.app.db.entities.LessonEntity;
import lv.id.arseniuss.linguae.app.db.entities.LessonTaskCrossref;
import lv.id.arseniuss.linguae.app.db.entities.LessonTheoryCrossref;
import lv.id.arseniuss.linguae.app.db.entities.LicenseEntity;
import lv.id.arseniuss.linguae.app.db.entities.SessionResultEntity;
import lv.id.arseniuss.linguae.app.db.entities.SettingEntity;
import lv.id.arseniuss.linguae.app.db.entities.TaskEntity;
import lv.id.arseniuss.linguae.app.db.entities.TaskResultEntity;
import lv.id.arseniuss.linguae.app.db.entities.TheoryChapterCrossref;
import lv.id.arseniuss.linguae.app.db.entities.TheoryEntity;
import lv.id.arseniuss.linguae.app.db.entities.TrainingCategoryEntity;
import lv.id.arseniuss.linguae.app.db.entities.TrainingEntity;
import lv.id.arseniuss.linguae.app.db.entities.TrainingTaskCrossref;
import lv.id.arseniuss.linguae.app.db.entities.VocabularyEntity;

@Database(version = 1, exportSchema = false,
        entities = {LessonEntity.class, LessonTaskCrossref.class, SettingEntity.class,
                TaskEntity.class, TrainingEntity.class, TrainingTaskCrossref.class,
                SessionResultEntity.class, TaskResultEntity.class, ChapterEntity.class,
                TheoryEntity.class, TheoryChapterCrossref.class, LessonTheoryCrossref.class,
                ConfigEntity.class, TrainingCategoryEntity.class, LicenseEntity.class,
                LessonChapterCrossref.class, BugReportEntity.class, CheckpointEntity.class,
                VocabularyEntity.class})
@TypeConverters({DatabaseConverters.class})
public abstract class LanguageDatabase extends RoomDatabase {
    private static LanguageDatabase _instance;
    private String _language;

    public static synchronized LanguageDatabase GetInstance(Context context, String language) {
        if (_instance == null || !Objects.equals(_instance._language, language)) {
            _instance =
                    Room.databaseBuilder(context.getApplicationContext(), LanguageDatabase.class,
                            language).fallbackToDestructiveMigration().build();
            _instance._language = language;
        }
        return _instance;
    }

    public static LanguageDatabase GetInstance(Context context) {
        return GetInstance(context, Configuration.GetLanguageCode());
    }

    public abstract SummaryDataAccess GetSummaryDataAccess();

    public abstract UpdateDataAccess GetUpdateDataAccess();

    public abstract LessonDataAccess GetLessonsDataAccess();

    public abstract TrainingDataAccess GetTrainingsDataAccess();

    public abstract TaskDataAccess GetTaskDataAccess();

    public abstract SessionDataAccess GetSessionDataAccess();

    public abstract TheoryDataAccess GetTheoryDataAccess();

    public abstract LicenseDataAccess GetLicenseDataAccess();

    public abstract BugReportDataAccess GetBugReportDataAccess();

    public abstract CommonDataAccess GetCommonDataAccess();
}
