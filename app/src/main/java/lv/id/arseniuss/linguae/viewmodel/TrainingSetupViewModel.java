package lv.id.arseniuss.linguae.viewmodel;

import static java.util.stream.Collectors.groupingBy;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.Constants;
import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.TrainingDataAccess;
import lv.id.arseniuss.linguae.db.entities.TrainingConfig;

public class TrainingSetupViewModel extends AndroidViewModel {
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _language = _sharedPreferences.getString(Constants.PreferenceLanguageKey, "");
    private final TrainingDataAccess _trainingDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetTrainingsDataAccess();

    private final MutableLiveData<List<TrainingConfigViewModel>> _trainingConfig =
            new MutableLiveData<>(new ArrayList<>());

    public TrainingSetupViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<List<TrainingConfigViewModel>> TrainingConfigs() { return _trainingConfig; }

    public void Load(String trainingId) {
        Disposable d = _trainingDataAccess.GetTrainingConfig(trainingId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(trainingConfigs -> {
                    Map<TaskType, List<TrainingConfig>> collect =
                            trainingConfigs.stream().collect(groupingBy(TrainingConfig::getType));

                    _trainingConfig.setValue(collect.entrySet()
                            .stream()
                            .map(e -> new TrainingConfigViewModel(e.getKey(), e.getValue()))
                            .collect(Collectors.toList()));
                });
    }

    public static class TrainingConfigViewModel extends BaseObservable {
        private final MutableLiveData<String> _key = new MutableLiveData<>("");
        private final MutableLiveData<List<SelectionViewModel>> _categories = new MutableLiveData<>(new ArrayList<>());
        private final MutableLiveData<List<SelectionViewModel>> _descriptions =
                new MutableLiveData<>(new ArrayList<>());

        public TrainingConfigViewModel(TaskType key, List<TrainingConfig> configs) {
            _key.setValue(key.GetName());

            _categories.setValue(configs.stream()
                    .map(c -> c.Category)
                    .distinct()
                    .map(SelectionViewModel::new)
                    .collect(Collectors.toList()));
            _descriptions.setValue(configs.stream()
                    .map(c -> c.Description)
                    .distinct()
                    .map(SelectionViewModel::new)
                    .collect(Collectors.toList()));
        }

        public MutableLiveData<String> Key() { return _key; }

        public MutableLiveData<List<SelectionViewModel>> Categories() { return _categories; }

        public MutableLiveData<List<SelectionViewModel>> Descriptions() { return _descriptions; }

        public static class SelectionViewModel {
            private final MutableLiveData<String> _data = new MutableLiveData<>("");
            private final MutableLiveData<Boolean> _selected = new MutableLiveData<>(true);

            public SelectionViewModel(String data) {
                _data.setValue(data);
            }

            public MutableLiveData<Boolean> Selected() { return _selected; }

            public MutableLiveData<String> Data() { return _data; }
        }
    }
}
