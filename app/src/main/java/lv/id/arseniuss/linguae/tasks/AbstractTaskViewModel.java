package lv.id.arseniuss.linguae.tasks;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.Utilities;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;

public abstract class AbstractTaskViewModel extends AndroidViewModel {

    protected final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    protected final String _language =
            _sharedPreferences.getString(getApplication().getString(R.string.PreferenceLanguageKey), "");
    protected final Boolean _ignoreMacrons =
            _sharedPreferences.getBoolean(getApplication().getString(R.string.PreferenceIgnoreMacronsKey), false);
    protected SessionTaskData _taskResult;

    protected MutableLiveData<Boolean> _isValidated = new MutableLiveData<>(false);

    public AbstractTaskViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<Boolean> IsValidated() { return _isValidated; }

    public void Load(SessionTaskData task) {
        _taskResult = task;
    }

    public abstract boolean Validate();

    public String StripAccents(String text) {
        return _ignoreMacrons ? Utilities.StripAccents(text) : text;
    }
}
