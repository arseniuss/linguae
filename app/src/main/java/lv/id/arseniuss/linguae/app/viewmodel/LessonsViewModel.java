package lv.id.arseniuss.linguae.app.viewmodel;

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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.app.Constants;
import lv.id.arseniuss.linguae.app.Settings;
import lv.id.arseniuss.linguae.app.Utilities;
import lv.id.arseniuss.linguae.app.db.LanguageDatabase;
import lv.id.arseniuss.linguae.app.db.dataaccess.LessonDataAccess;
import lv.id.arseniuss.linguae.app.ui.BindingAdapters;

public class LessonsViewModel extends AndroidViewModel {

    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _language =
            _sharedPreferences.getString(Constants.PreferenceLanguageCodeKey, "");
    private final LessonDataAccess _lessonDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetLessonsDataAccess();

    private final MutableLiveData<List<SectionViewModel>> _sections =
            new MutableLiveData<>(new ArrayList<>());
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

    public LiveData<List<SectionViewModel>> Sections() {
        return _sections;
    }

    public LiveData<List<EntryViewModel>> Lessons() {
        return _lessons;
    }

    public void Load() {
        Disposable d = _lessonDataAccess.GetLessons()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::acceptLessons, this::handleError);
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

    private void acceptLessons(List<LessonDataAccess.LessonWithCount> lessons) {
        Map<String, List<LessonDataAccess.LessonWithCount>> sections =
                lessons.stream().collect(Collectors.groupingBy(l -> l.Lesson.Section));

        _sections.setValue(sections.entrySet()
                .stream()
                .map(s -> new SectionViewModel(s.getKey(), s.getValue()
                        .stream()
                        .map(EntryViewModel::new)
                        .collect(Collectors.toList())))
                .sorted(Comparator.comparing(e -> e.Index()))
                .collect(Collectors.toList()));
    }

    public void SetSelectedSection(String tabName) {
        Optional<SectionViewModel> section = Objects.requireNonNull(_sections.getValue())
                .stream().filter(s -> Objects.equals(s.TabName(), tabName)).findAny();

        section.ifPresent(sectionViewModel -> _lessons.setValue(
                sectionViewModel.Lessons()));
    }

    public static class SectionViewModel extends BaseObservable
            implements BindingAdapters.TabViewModel {
        private final String _name;
        private final List<EntryViewModel> _lessons;

        private int _index;

        public SectionViewModel(String name, List<EntryViewModel> lessons) {
            _name = name;
            _lessons = lessons;

            lessons.stream()
                    .min(Comparator.comparing(l -> l._lesson.Lesson.Index))
                    .ifPresent(l -> _index = l._lesson.Lesson.Index);
        }

        @Override
        public String TabName() {
            return _name;
        }

        public int Index() {
            return _index;
        }

        public List<EntryViewModel> Lessons() {
            return _lessons;
        }
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