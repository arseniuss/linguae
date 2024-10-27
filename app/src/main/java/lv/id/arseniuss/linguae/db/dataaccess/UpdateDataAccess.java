package lv.id.arseniuss.linguae.db.dataaccess;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import lv.id.arseniuss.linguae.data.LanguageDataParser;
import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.entities.Chapter;
import lv.id.arseniuss.linguae.db.entities.Config;
import lv.id.arseniuss.linguae.db.entities.Lesson;
import lv.id.arseniuss.linguae.db.entities.LessonTaskCrossref;
import lv.id.arseniuss.linguae.db.entities.LessonTheoryCrossref;
import lv.id.arseniuss.linguae.db.entities.License;
import lv.id.arseniuss.linguae.db.entities.Setting;
import lv.id.arseniuss.linguae.db.entities.Task;
import lv.id.arseniuss.linguae.db.entities.Theory;
import lv.id.arseniuss.linguae.db.entities.TheoryChapterCrossref;
import lv.id.arseniuss.linguae.db.entities.Training;
import lv.id.arseniuss.linguae.db.entities.TrainingCategory;
import lv.id.arseniuss.linguae.db.entities.TrainingTaskCrossref;

@Dao
public abstract class UpdateDataAccess {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract Completable InsertSettings(Collection<Setting> settings);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract Completable InsertConfig(List<Config> configs);

    @Insert
    protected abstract Completable InsertTrainings(List<Training> trainings);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract Completable InsertTrainingCategories(List<TrainingCategory> categories);

    @Query("DELETE FROM training")
    protected abstract Completable DeleteTrainings();

    @Insert
    protected abstract Completable InsertTasks(List<Task> tasks);

    @Query("DELETE FROM task")
    protected abstract Completable DeleteTasks();

    @Insert
    protected abstract Completable InsertTrainingTaskCrossref(List<TrainingTaskCrossref> trainingTaskCrossrefs);

    @Insert
    protected abstract Completable InsertLessonTaskCrossref(List<LessonTaskCrossref> lessonTaskCrossrefs);

    @Query("DELETE FROM training_task")
    protected abstract Completable DeleteTrainingsTaskCrossref();

    @Query("DELETE FROM lesson_task")
    protected abstract Completable DeleteLessonTaskCrossref();

    @Insert
    protected abstract Completable InsertLessons(List<Lesson> lessons);

    @Query("DELETE FROM lesson")
    protected abstract Completable DeleteLessons();

    @Query("DELETE FROM chapter")
    protected abstract Completable DeleteChapters();

    @Query("DELETE FROM theory")
    protected abstract Completable DeleteTheory();

    @Query("DELETE FROM theory_chapter")
    protected abstract Completable DeleteTheoryChapterCrossrefs();

    @Query("DELETE FROM license")
    protected abstract Completable DeleteLicenses();

    @Insert
    protected abstract Completable InsertLicenses(List<License> licenses);

    @Insert
    protected abstract Completable InsertChapters(List<Chapter> chapters);

    @Insert
    protected abstract Completable InsertTheory(List<Theory> theories);

    @Insert
    protected abstract Completable InsertTheoryChapterCrossref(List<TheoryChapterCrossref> crossrefs);

    @Insert
    protected abstract Completable InsertLessonTheoryCrossref(List<LessonTheoryCrossref> crossrefs);

    @Query("DELETE FROM lesson_theory")
    public abstract Completable DeleteLessonTheoryCrossref();

    @Query("SELECT value FROM config WHERE `key` = 'version'")
    public abstract Maybe<String> GetVersion();

    public Completable PerformUpdate(LanguageDataParser.ParserData data)
    {
        return InsertSettings(data.Settings.values())
                // -----
                .andThen(InsertConfig(data.Config.entrySet()
                        .stream()
                        .map(c -> new Config(c.getKey(), c.getValue()))
                        .collect(Collectors.toList())))
                // -----
                .andThen(DeleteTrainingsTaskCrossref())
                .andThen(DeleteTrainings())
                .andThen(DeleteLessonTaskCrossref())
                .andThen(DeleteLessonTheoryCrossref())
                .andThen(DeleteLessons())
                .andThen(DeleteTasks())
                .andThen(DeleteTheoryChapterCrossrefs())
                .andThen(DeleteTheory())
                .andThen(DeleteChapters())
                .andThen(DeleteLicenses())
                // -----
                .andThen(InsertLicenses(data.Licences))
                .andThen(InsertChapters(
                        data.Theory.values().stream().flatMap(t -> t.Chapters.stream()).collect(Collectors.toList())))
                .andThen(InsertTheory(data.Theory.values().stream().map(t -> t.Theory).collect(Collectors.toList())))
                .andThen(InsertTheoryChapterCrossref(data.Theory.values()
                        .stream()
                        .flatMap(t -> t.Chapters.stream()
                                .map(c -> new TheoryChapterCrossref(t.Theory.Id, c.Id))
                                .collect(Collectors.toList())
                                .stream())
                        .collect(Collectors.toList())))
                // -----
                .andThen(InsertTasks(
                        data.Trainings.values().stream().flatMap(t -> t.Tasks.stream()).collect(Collectors.toList())))
                .andThen(InsertTrainings(
                        data.Trainings.values().stream().map(t -> t.Training).collect(Collectors.toList())))
                .andThen(InsertTrainingCategories(data.TrainingCategories))
                .andThen(InsertTrainingTaskCrossref(data.Trainings.values()
                        .stream()
                        .flatMap(t -> t.Tasks.stream()
                                .map(m -> new TrainingTaskCrossref(t.Training.Id, m.Id))
                                .collect(Collectors.toList())
                                .stream())
                        .collect(Collectors.toList())))
                // -----
                .andThen(InsertLessons(data.Lessons.values().stream().map(l -> l.Lesson).collect(Collectors.toList())))
                .andThen(InsertTasks(data.Lessons.values()
                        .stream()
                        .flatMap(l -> l.Tasks.stream())
                        .filter(t -> t.Type != TaskType.UnknownTask)
                        .collect(Collectors.toList())))
                .andThen(InsertLessonTheoryCrossref(data.Lessons.values()
                        .stream()
                        .flatMap(l -> l.Theories.stream()
                                .map(t -> new LessonTheoryCrossref(l.Lesson.Id, t.Id))
                                .collect(Collectors.toList())
                                .stream())
                        .collect(Collectors.toList())))
                .andThen(InsertLessonTaskCrossref(data.Lessons.values()
                        .stream()
                        .flatMap(l -> l.Tasks.stream()
                                .map(t -> new LessonTaskCrossref(l.Lesson.Id, t.Id))
                                .collect(Collectors.toList())
                                .stream())
                        .collect(Collectors.toList())));
    }
}
