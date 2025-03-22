package lv.id.arseniuss.linguae.app.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.app.Configuration;
import lv.id.arseniuss.linguae.app.Constants;
import lv.id.arseniuss.linguae.app.Utilities;
import lv.id.arseniuss.linguae.app.db.LanguageDatabase;
import lv.id.arseniuss.linguae.app.db.dataaccess.TaskDataAccess;
import lv.id.arseniuss.linguae.app.db.entities.TaskEntity;
import lv.id.arseniuss.linguae.tasks.ChooseTask;
import lv.id.arseniuss.linguae.tasks.ConjugateTask;
import lv.id.arseniuss.linguae.tasks.DeclineTask;
import lv.id.arseniuss.linguae.tasks.SelectTask;
import lv.id.arseniuss.linguae.tasks.TranslateTask;

public class LessonSummaryViewModel extends AndroidViewModel {
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _language =
            _sharedPreferences.getString(Constants.PreferenceLanguageKey, "");
    private final TaskDataAccess _taskDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetTaskDataAccess();

    private final MutableLiveData<List<ItemMarkdownViewModel>> _vocabulary =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> _hasVocabulary = new MutableLiveData<>(false);
    private final MutableLiveData<List<String>> _sentences =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> _hasSentences = new MutableLiveData<>(false);

    public LessonSummaryViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<List<ItemMarkdownViewModel>> Vocabulary() {
        return _vocabulary;
    }

    public MutableLiveData<Boolean> HasVocabulary() {
        return _hasVocabulary;
    }

    public MutableLiveData<List<String>> Sentences() {
        return _sentences;
    }

    public MutableLiveData<Boolean> HasSentences() {
        return _hasSentences;
    }

    private void parseResult(List<TaskEntity> tasks) {
        final String languageCode = Configuration.GetLanguageCode();

        List<ItemMarkdownViewModel> vocabulary = new ArrayList<>();
        List<String> sentences = new ArrayList<>();

        if (languageCode != null) {
            for (TaskEntity task : tasks) {
                if (task.Id.endsWith("-" + languageCode)) {
                    String word = null;
                    String sentence = null;

                    switch (task.Type) {
                        case ChooseTask:
                            ChooseTask chooseTask = (ChooseTask) task.Data;

                            word = chooseTask.Word;
                            break;
                        case DeclineTask:
                            DeclineTask declineTask = (DeclineTask) task.Data;

                            word = Arrays.stream(declineTask.Word.split(","))
                                    .map(String::trim)
                                    .filter(w -> !w.isEmpty())
                                    .findFirst()
                                    .orElse(null);
                            break;
                        case ConjugateTask:
                            ConjugateTask conjugateTask = (ConjugateTask) task.Data;

                            word = conjugateTask.Verb;
                            break;
                        case TranslateTask:
                            TranslateTask translateTask = (TranslateTask) task.Data;

                            sentence = translateTask.Text;
                            break;
                        case SelectTask:
                            SelectTask selectTask = (SelectTask) task.Data;

                            sentence = String.join(" ", selectTask.Sentence)
                                    .replaceAll("<", "")
                                    .replaceAll("\\.", "")
                                    .replaceAll(">", "");
                            break;
                    }

                    if (word != null) {
                        final String markdown =
                                "[" + word + "](wikt:" + Utilities.StripAccents(word) + ")";

                        if (vocabulary.stream()
                                .noneMatch(w -> Objects.equals(w.Markdown, markdown)))
                            vocabulary.add(new ItemMarkdownViewModel(markdown));
                    }
                    if (sentence != null && !sentences.contains(sentence)) {
                        sentences.add(sentence);
                    }
                }
            }

            if (!vocabulary.isEmpty()) {
                _hasVocabulary.setValue(true);
                vocabulary.sort(Comparator.comparing(o -> o.Markdown));
                _vocabulary.setValue(vocabulary);
            }

            if (!sentences.isEmpty()) {
                _hasSentences.setValue(true);
                sentences.sort(String::compareTo);
                _sentences.setValue(sentences);
            }
        }
    }

    public void LoadLesson(String lessonNo) {
        Disposable d = _taskDataAccess.GetTasksByLesson(lessonNo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::parseResult);
    }
}
