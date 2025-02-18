package lv.id.arseniuss.linguae.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.Constants;
import lv.id.arseniuss.linguae.Settings;
import lv.id.arseniuss.linguae.Utilities;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.LessonDataAccess;

public class LessonsViewModel extends AndroidViewModel {

    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _language =
            _sharedPreferences.getString(Constants.PreferenceLanguageKey, "");
    private final LessonDataAccess _lessonDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetLessonsDataAccess();

    private final MutableLiveData<List<EntryViewModel>> _lessons;
    private final MutableLiveData<Boolean> _hasError = new MutableLiveData<>(false);
    private final MutableLiveData<String> _error = new MutableLiveData<>("");

    public LessonsViewModel(@NonNull Application application) {
        super(application);

        _lessons = new MutableLiveData<>(new ArrayList<>());

        Load();
    }

    public MutableLiveData<Boolean> HasError() {
        return _hasError;
    }

    public MutableLiveData<String> GetError() {
        return _error;
    }

    public LiveData<List<EntryViewModel>> Data() {
        return _lessons;
    }

    public void Load() {
        Disposable d = _lessonDataAccess.GetLessons()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lessons -> {
                    _lessons.setValue(
                            lessons.stream().map(EntryViewModel::new).collect(Collectors.toList()));
                }, this::handleError);

    }

    private void handleError(Throwable throwable) {
        _hasError.setValue(true);
        _error.setValue(throwable.getMessage());
    }

    public LessonDataAccess.LessonWithCount GetLesson(int selection) {
        LessonDataAccess.LessonWithCount res = null;

        try {
            EntryViewModel viewModel = _lessons.getValue().get(selection);

            res = viewModel._lesson;
        } catch (NullPointerException ignore) {

        }

        return res;
    }

    public static class EntryViewModel extends BaseObservable {
        private final LessonDataAccess.LessonWithCount _lesson;

        public EntryViewModel(LessonDataAccess.LessonWithCount lesson) {
            _lesson = lesson;
        }

        public String getNo() {
            return _lesson.Lesson.Id;
        }

        @Bindable("Name")
        public String getName() {
            return Settings.IgnoreMacrons ? Utilities.StripAccents(_lesson.Lesson.Name) :
                    _lesson.Lesson.Name;
        }

        public boolean HasTheory() {
            return _lesson.TheoryCount > 0;
        }

        @Bindable("Filename")
        public String getFilename() {
            return _lesson.Lesson.Id;
        }

        @Bindable("Description")
        public String getDescription() {
            return _lesson.Lesson.Description;
        }

        @Bindable("TaskCount")
        public String getTaskCount() {
            return String.valueOf(_lesson.TaskCount);
        }

        @Bindable("TheoryCount")
        public String getTheoryCount() {
            return String.valueOf(_lesson.TheoryCount);
        }
    }
}