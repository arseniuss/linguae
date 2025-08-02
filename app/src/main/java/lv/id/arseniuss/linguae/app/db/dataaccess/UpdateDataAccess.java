package lv.id.arseniuss.linguae.app.db.dataaccess;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import lv.id.arseniuss.linguae.app.db.entities.ChapterEntity;
import lv.id.arseniuss.linguae.app.db.entities.ConfigEntity;
import lv.id.arseniuss.linguae.app.db.entities.LessonChapterCrossref;
import lv.id.arseniuss.linguae.app.db.entities.LessonEntity;
import lv.id.arseniuss.linguae.app.db.entities.LessonTaskCrossref;
import lv.id.arseniuss.linguae.app.db.entities.LessonTheoryCrossref;
import lv.id.arseniuss.linguae.app.db.entities.LicenseEntity;
import lv.id.arseniuss.linguae.app.db.entities.SettingEntity;
import lv.id.arseniuss.linguae.app.db.entities.TaskEntity;
import lv.id.arseniuss.linguae.app.db.entities.TheoryChapterCrossref;
import lv.id.arseniuss.linguae.app.db.entities.TheoryEntity;
import lv.id.arseniuss.linguae.app.db.entities.TrainingCategoryEntity;
import lv.id.arseniuss.linguae.app.db.entities.TrainingEntity;
import lv.id.arseniuss.linguae.app.db.entities.TrainingTaskCrossref;
import lv.id.arseniuss.linguae.enumerators.TaskType;

@Dao
public abstract class UpdateDataAccess {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract Completable InsertSettings(Collection<SettingEntity> settingEntities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract Completable InsertConfig(List<ConfigEntity> configs);

    @Insert
    protected abstract Completable InsertTrainings(List<TrainingEntity> trainings);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract Completable InsertTrainingCategories(
            List<TrainingCategoryEntity> categories);

    @Query("DELETE FROM training")
    protected abstract Completable DeleteTrainings();

    @Insert
    protected abstract Completable InsertTasks(List<TaskEntity> taskEntities);

    @Query("DELETE FROM task")
    protected abstract Completable DeleteTasks();

    @Insert
    protected abstract Completable InsertTrainingTaskCrossref(
            List<TrainingTaskCrossref> trainingTaskCrossrefs);

    @Insert
    protected abstract Completable InsertLessonTaskCrossref(
            List<LessonTaskCrossref> lessonTaskCrossrefs);

    @Query("DELETE FROM training_task")
    protected abstract Completable DeleteTrainingsTaskCrossref();

    @Query("DELETE FROM lesson_task")
    protected abstract Completable DeleteLessonTaskCrossref();

    @Insert
    protected abstract Completable InsertLessons(List<LessonEntity> lessonEntities);

    @Query("DELETE FROM lesson")
    protected abstract Completable DeleteLessons();

    @Query("DELETE FROM chapter")
    protected abstract Completable DeleteChapters();

    @Query("DELETE FROM lesson_chapter")
    protected abstract Completable DeleteLessonChapters();

    @Query("DELETE FROM theory")
    protected abstract Completable DeleteTheory();

    @Query("DELETE FROM theory_chapter")
    protected abstract Completable DeleteTheoryChapter();

    @Query("DELETE FROM license")
    protected abstract Completable DeleteLicenses();

    @Insert
    protected abstract Completable InsertLicenses(List<LicenseEntity> licens);

    @Insert
    protected abstract Completable InsertChapters(List<ChapterEntity> chapterEntities);

    @Insert
    protected abstract Completable InsertTheory(List<TheoryEntity> theories);

    @Insert
    protected abstract Completable InsertTheoryChapterCrossref(
            List<TheoryChapterCrossref> crossrefs);

    @Insert
    protected abstract Completable InsertLessonTheoryCrossref(List<LessonTheoryCrossref> crossrefs);

    @Insert
    protected abstract Completable InsertLessonChapterCrossref(
            List<LessonChapterCrossref> crossrefs);

    @Query("DELETE FROM lesson_theory")
    public abstract Completable DeleteLessonTheoryCrossref();

    @Query("SELECT value FROM config WHERE `key` = 'version'")
    public abstract Maybe<String> GetVersion();

    public Completable PerformUpdate(
            lv.id.arseniuss.linguae.parsers.LanguageDataParser.ParserData data) {
        return InsertSettings(data.Settings.values()
                .stream()
                .map(SettingEntity::new)
                .collect(Collectors.toList()))
                // -----
                .andThen(InsertConfig(data.Config.entrySet()
                        .stream()
                        .map(c -> new ConfigEntity(c.getKey(), c.getValue()))
                        .collect(Collectors.toList())))
                // -----
                .andThen(DeleteTrainingsTaskCrossref())
                .andThen(DeleteTrainings())
                .andThen(DeleteLessonTaskCrossref())
                .andThen(DeleteLessonTheoryCrossref())
                .andThen(DeleteLessons())
                .andThen(DeleteTasks())
                .andThen(DeleteLessonChapters())
                .andThen(DeleteTheoryChapter())
                .andThen(DeleteTheory())
                .andThen(DeleteChapters())
                .andThen(DeleteLicenses())
                // -----
                .andThen(InsertLicenses(data.Licences.stream()
                        .map(LicenseEntity::new)
                        .collect(Collectors.toList())))
                .andThen(InsertChapters(data.Theory.values()
                        .stream()
                        .flatMap(t -> t.Chapters.stream().map(ChapterEntity::new))
                        .collect(Collectors.toList())))
                .andThen(InsertChapters(data.Lessons.values()
                        .stream()
                        .flatMap(t -> t.Chapters.values().stream().map(ChapterEntity::new))
                        .collect(Collectors.toList())))
                .andThen(InsertTheory(data.Theory.values()
                        .stream()
                        .map(TheoryEntity::new)
                        .collect(Collectors.toList())))
                .andThen(InsertTheoryChapterCrossref(data.Theory.values()
                        .stream()
                        .flatMap(t -> t.Chapters.stream()
                                .map(c -> new TheoryChapterCrossref(t.Id, c.Id))
                                .collect(Collectors.toList())
                                .stream())
                        .collect(Collectors.toList())))
                // -----
                .andThen(InsertTasks(data.Trainings.values()
                        .stream()
                        .flatMap(t -> t.Tasks.stream().map(TaskEntity::new))
                        .collect(Collectors.toList())))
                .andThen(InsertTrainings(data.Trainings.values()
                        .stream()
                        .map(TrainingEntity::new)
                        .collect(Collectors.toList())))
                .andThen(InsertTrainingCategories(data.TrainingCategories.stream()
                        .map(TrainingCategoryEntity::new)
                        .collect(Collectors.toList())))
                .andThen(InsertTrainingTaskCrossref(data.Trainings.values()
                        .stream()
                        .flatMap(t -> t.Tasks.stream()
                                .map(m -> new TrainingTaskCrossref(t.Id, m.Id))
                                .collect(Collectors.toList())
                                .stream())
                        .collect(Collectors.toList())))
                // -----
                .andThen(InsertLessons(data.Lessons.values()
                        .stream()
                        .map(LessonEntity::new)
                        .collect(Collectors.toList())))
                .andThen(InsertTasks(data.Lessons.values()
                        .stream()
                        .flatMap(l -> l.Tasks.stream().map(TaskEntity::new))
                        .filter(t -> t.Type != TaskType.UnknownTask)
                        .collect(Collectors.toList())))
                .andThen(InsertLessonChapterCrossref(data.Lessons.values()
                        .stream()
                        .flatMap(l -> l.Chapters.values().stream()
                                .map(c -> new LessonChapterCrossref(l.Id, c.Id))
                                .collect(Collectors.toList())
                                .stream())
                        .collect(Collectors.toList())))
                .andThen(InsertLessonTheoryCrossref(data.Lessons.values()
                        .stream()
                        .flatMap(l -> l.Theories.stream()
                                .map(t -> new LessonTheoryCrossref(l.Id, t.Id))
                                .collect(Collectors.toList())
                                .stream())
                        .collect(Collectors.toList())))
                .andThen(InsertLessonTaskCrossref(data.Lessons.values()
                        .stream()
                        .flatMap(l -> l.Tasks.stream()
                                .map(t -> new LessonTaskCrossref(l.Id, t.Id))
                                .collect(Collectors.toList())
                                .stream())
                        .collect(Collectors.toList())));
    }
}
