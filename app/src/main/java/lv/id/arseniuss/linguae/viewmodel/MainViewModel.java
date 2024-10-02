package lv.id.arseniuss.linguae.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import lv.id.arseniuss.linguae.R;

public class MainViewModel extends AndroidViewModel implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _languageKey = getApplication().getString(R.string.PreferenceLanguageKey);

    private final MutableLiveData<String> _language;

    public MainViewModel(@NonNull Application application) {
        super(application);

        _sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        _language = new MutableLiveData<>(_sharedPreferences.getString(_languageKey, ""));
    }

    public MutableLiveData<String> GetLanguage() {
        return _language;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        _language.setValue(
                _sharedPreferences.getString(getApplication().getString(R.string.PreferenceLanguageKey), ""));
    }
}
