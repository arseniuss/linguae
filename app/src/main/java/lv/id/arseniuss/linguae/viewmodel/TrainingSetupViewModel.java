package lv.id.arseniuss.linguae.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import lv.id.arseniuss.linguae.Constants;
import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.TrainingDataAccess;

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

    }

    public static class TrainingConfigViewModel extends BaseObservable {
        private final MutableLiveData<String> _key = new MutableLiveData<>("");
        private final MutableLiveData<List<SelectionViewModel>> _categories = new MutableLiveData<>(new ArrayList<>());
        private final MutableLiveData<List<SelectionViewModel>> _descriptions =
                new MutableLiveData<>(new ArrayList<>());

        public TrainingConfigViewModel(TaskType key) {
            _key.setValue(key.GetName());
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
