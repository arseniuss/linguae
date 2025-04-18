package lv.id.arseniuss.linguae.app.tasks;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import lv.id.arseniuss.linguae.app.Constants;
import lv.id.arseniuss.linguae.app.Settings;
import lv.id.arseniuss.linguae.app.Utilities;
import lv.id.arseniuss.linguae.app.tasks.entities.SessionTaskData;

public abstract class AbstractTaskViewModel extends AndroidViewModel {

    protected final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    protected final String _language =
            _sharedPreferences.getString(Constants.PreferenceLanguageCodeKey, "");
    protected final Boolean _noKeyboard =
            _sharedPreferences.getBoolean(Constants.PreferenceNoKeyboardKey, false);
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

    public String StripBrackets(String text) {
        switch (text.charAt(0)) {
            case '<':
                text = text.substring(1);
                if (text.charAt(text.length() - 1) == '>')
                    text = text.substring(0, text.length() - 1);
                break;
        }

        return text;
    }
}
