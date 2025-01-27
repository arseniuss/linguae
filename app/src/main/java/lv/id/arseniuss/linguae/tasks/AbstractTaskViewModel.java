package lv.id.arseniuss.linguae.tasks;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import lv.id.arseniuss.linguae.Constants;
import lv.id.arseniuss.linguae.Settings;
import lv.id.arseniuss.linguae.Utilities;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;

public abstract class AbstractTaskViewModel extends AndroidViewModel {

    protected final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    protected final String _language = _sharedPreferences.getString(Constants.PreferenceLanguageKey, "");
    protected SessionTaskData _taskResult;

    protected MutableLiveData<Boolean> _isValidated = new MutableLiveData<>(false);

    public AbstractTaskViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<Boolean> IsValidated() {
        return _isValidated;
    }

    public void Load(SessionTaskData task) {
        _taskResult = task;
    }

    public abstract boolean Validate();

    public String StripAccents(String text) {
        return Settings.IgnoreMacrons ? Utilities.StripAccents(text) : text;
    }
}
