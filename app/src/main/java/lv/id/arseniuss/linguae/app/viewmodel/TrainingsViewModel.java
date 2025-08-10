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
import lv.id.arseniuss.linguae.app.db.dataaccess.MainDataAccess;
import lv.id.arseniuss.linguae.app.db.dataaccess.TrainingDataAccess;
import lv.id.arseniuss.linguae.app.ui.BindingAdapters;

public class TrainingsViewModel extends AndroidViewModel {

    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _language =
            _sharedPreferences.getString(Constants.PreferenceLanguageCodeKey, "");
    private final TrainingDataAccess _trainingDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetTrainingsDataAccess();
    private final MainDataAccess _mainDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetMainDataAccess();

    private final MutableLiveData<List<EntryViewModel>> _trainings;
    private final MutableLiveData<List<SectionViewModel>> _sections =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> _hasError = new MutableLiveData<>(false);
    private final MutableLiveData<String> _error = new MutableLiveData<>("");

    public TrainingsViewModel(@NonNull Application application) {
        super(application);

        _trainings = new MutableLiveData<>(new ArrayList<>());

        loadData();
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

    public LiveData<List<EntryViewModel>> Data() {
        return _trainings;
    }

    public String GetTraining(int selection) {
        String res = "";

        try {
            EntryViewModel entryViewModel = _trainings.getValue().get(selection);

            res = entryViewModel.getId();
        } catch (NullPointerException ignore) {

        }

        return res;
    }

    void loadData() {
        Disposable d = _trainingDataAccess.GetTrainings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::acceptTrainings, this::handleError);

    }

    private void handleError(Throwable throwable) {
        _hasError.setValue(true);
        _error.setValue(throwable.getMessage());
    }

    private void acceptTrainings(List<TrainingDataAccess.TrainingWithCount> trainings) {
        Map<String, List<TrainingDataAccess.TrainingWithCount>> sections =
                trainings.stream().collect(Collectors.groupingBy(t -> t.Training.Section));

        _sections.setValue(sections.entrySet()
                .stream()
                .map(s -> new SectionViewModel(s.getKey(), s.getValue()
                        .stream()
                        .map(EntryViewModel::new)
                        .collect(Collectors.toList())))
                .sorted(Comparator.comparing(SectionViewModel::Index))
                .collect(Collectors.toList()));
    }

    public void SetSelectedSection(String tabName) {
        Optional<SectionViewModel> section = Objects.requireNonNull(_sections.getValue())
                .stream().filter(s -> Objects.equals(s.TabName(), tabName)).findAny();

        section.ifPresent(sectionViewModel -> _trainings.setValue(
                sectionViewModel.Trainings()));
    }

    public static class SectionViewModel extends BaseObservable
            implements BindingAdapters.TabViewModel {
        private final String _name;
        private final List<EntryViewModel> _trainings;

        private int _index;

        public SectionViewModel(String name, List<EntryViewModel> trainings) {
            _name = name;
            _trainings = trainings;

            trainings.stream()
                    .min(Comparator.comparing(t -> t._training.Training.Index))
                    .ifPresent(t -> _index = t._training.Training.Index);
        }

        @Override
        public String TabName() {
            return _name;
        }

        public int Index() {
            return _index;
        }

        public List<EntryViewModel> Trainings() {
            return _trainings;
        }
    }

    public static class EntryViewModel extends BaseObservable {
        private final TrainingDataAccess.TrainingWithCount _training;

        public EntryViewModel(TrainingDataAccess.TrainingWithCount training) {
            _training = training;
        }

        public String getId() {
            return _training.Training.Id;
        }

        @Bindable("Name")
        public String getName() {
            return Settings.IgnoreMacrons ? Utilities.StripAccents(_training.Training.Name) :
                    _training.Training.Name;
        }

        @Bindable("Description")
        public String getDescription() {
            return _training.Training.Description;
        }

        @Bindable("TaskCount")
        public String getTaskCount() {
            return String.valueOf(_training.TaskCount);
        }

        @Bindable("CategoryCount")
        public int getCategoryCount() {
            return _training.CategoryCount;
        }

    }
}