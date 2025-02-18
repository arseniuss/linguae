package lv.id.arseniuss.linguae.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.Constants;
import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.TaskDataAccess;
import lv.id.arseniuss.linguae.db.dataaccess.TrainingDataAccess;
import lv.id.arseniuss.linguae.db.entities.TrainingCategory;

public class TrainingSetupViewModel extends AndroidViewModel {
    private static String _trainingId = "";
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _language =
            _sharedPreferences.getString(Constants.PreferenceLanguageKey, "");
    private final TrainingDataAccess _trainingDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetTrainingsDataAccess();
    private final MutableLiveData<List<TrainingTaskViewModel>> _tasks =
            new MutableLiveData<>(new ArrayList<>());

    public TrainingSetupViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<List<TrainingTaskViewModel>> Tasks() {
        return _tasks;
    }

    public void Load(String trainingId) {
        _trainingId = trainingId;

        Disposable d = _trainingDataAccess.GetCategories(trainingId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::parseData, error -> {
                });
    }

    public List<TaskDataAccess.TrainingCategory> GetTrainingCategories() {
        List<TrainingTaskViewModel> tasksValue = _tasks.getValue();

        assert tasksValue != null;

        return tasksValue.stream()
                .flatMap(t -> Objects.requireNonNull(t._categories.getValue()).stream())
                .filter(t -> t._checked.getValue())
                .map(t -> new TaskDataAccess.TrainingCategory(t.TrainingCategory.Task,
                        t.TrainingCategory.Category,
                        t.TrainingCategory.Description))
                .collect(Collectors.toList());
    }

    private void parseData(List<TrainingCategory> categories) {
        List<TrainingTaskViewModel> trainingTaskViewModels = new ArrayList<>();

        for (TaskType tt : TaskType.values()) {
            if (tt == TaskType.UnknownTask) continue;

            if (categories.stream().anyMatch(c -> c.Task == tt)) {
                TrainingTaskViewModel trainingTaskViewModel = new TrainingTaskViewModel(tt);

                List<TrainingCategoryViewModel> trainingCategoryViewModels = categories.stream()
                        .distinct()
                        .filter(c -> c.Task == tt)
                        .map(c -> new TrainingCategoryViewModel(_sharedPreferences, c))
                        .collect(Collectors.toList());

                trainingTaskViewModel.SetCategories(trainingCategoryViewModels);
                trainingTaskViewModels.add(trainingTaskViewModel);
            }
        }

        _tasks.setValue(trainingTaskViewModels);
    }

    public static class TrainingTaskViewModel extends BaseObservable {
        private final MutableLiveData<List<TrainingCategoryViewModel>> _categories =
                new MutableLiveData<>(new ArrayList<>());

        public TaskType TaskType;

        public TrainingTaskViewModel(TaskType tt) {
            this.TaskType = tt;
        }

        public String GetTaskName() {
            return this.TaskType.GetName();
        }

        public MutableLiveData<List<TrainingCategoryViewModel>> GetCategories() {
            return _categories;
        }

        public void SetCategories(List<TrainingCategoryViewModel> trainingCategoryViewModels) {
            _categories.setValue(trainingCategoryViewModels);
        }
    }

    public static class TrainingCategoryViewModel extends BaseObservable
            implements Observer<Boolean> {
        public final TrainingCategory TrainingCategory;

        private final SharedPreferences _sharedPreferences;
        private final MutableLiveData<Boolean> _checked;

        public TrainingCategoryViewModel(SharedPreferences sharedPreferences,
                                         TrainingCategory trainingCategory) {
            _sharedPreferences = sharedPreferences;
            TrainingCategory = trainingCategory;
            _checked = new MutableLiveData<>(sharedPreferences.getBoolean(GetKey(), true));
            _checked.observeForever(this);
        }

        private String GetKey() {
            return "training-category" + TrainingCategory.Id + "-" + _trainingId;
        }

        public MutableLiveData<Boolean> GetChecked() {
            return _checked;
        }

        public String GetCategory() {
            return TrainingCategory.Category + ", " + TrainingCategory.Description;
        }

        @Override
        public void onChanged(Boolean aBoolean) {
            _sharedPreferences.edit().putBoolean(GetKey(), aBoolean).apply();
        }
    }
}
