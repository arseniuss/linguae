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
import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.Utilities;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.TrainingDataAccess;

public class TrainingsViewModel extends AndroidViewModel {

    private final SharedPreferences _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            getApplication().getBaseContext());
    private final String _language = _sharedPreferences.getString(
            getApplication().getString(R.string.PreferenceLanguageKey), "");
    private final TrainingDataAccess _trainingDataAccess = LanguageDatabase.GetInstance(getApplication(), _language)
            .GetTrainingsDataAccess();
    private final Boolean _ignoreMacrons = _sharedPreferences.getBoolean(
            getApplication().getString(R.string.PreferenceIgnoreMacronsKey), false);
    private final MutableLiveData<List<EntryViewModel>> _trainings;

    public TrainingsViewModel(@NonNull Application application) {
        super(application);

        _trainings = new MutableLiveData<>(new ArrayList<>());

        loadData();
    }

    public LiveData<List<EntryViewModel>> Data() { return _trainings; }

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
                .subscribe(trainings -> {
                    _trainings.setValue(trainings.stream()
                            .map(t -> new EntryViewModel(t, _ignoreMacrons))
                            .collect(Collectors.toList()));
                });

    }

    public static class EntryViewModel extends BaseObservable {
        private final TrainingDataAccess.TrainingWithCount _training;
        private final Boolean _ignoreMacrons;

        public EntryViewModel(TrainingDataAccess.TrainingWithCount training, Boolean ignoreMacrons) {
            _training = training;
            _ignoreMacrons = ignoreMacrons;
        }

        public String getId() { return _training.Training.Id; }

        @Bindable("Name")
        public String getName() {
            return _ignoreMacrons ? Utilities.StripAccents(_training.Training.Name) : _training.Training.Name;
        }

        @Bindable("Description")
        public String getDescription() { return _training.Training.Description; }

        @Bindable("TaskCount")
        public String getTaskCount() { return String.valueOf(_training.TaskCount); }

    }
}