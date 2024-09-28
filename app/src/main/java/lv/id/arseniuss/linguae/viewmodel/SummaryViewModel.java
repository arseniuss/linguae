package lv.id.arseniuss.linguae.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.SummaryDataAccess;

public class SummaryViewModel extends AndroidViewModel implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final MutableLiveData<Boolean> _hasSelectedLanguage = new MutableLiveData<Boolean>(false);
    private final MutableLiveData<String> _selectedLanguage = new MutableLiveData<>("");

    private final String _language =
            _sharedPreferences.getString(getApplication().getString(R.string.PreferenceLanguageKey), "");

    private final SummaryDataAccess _summaryDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetSummaryDataAccess();

    public SummaryViewModel(Application app) {
        super(app);
        _sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        Refresh();
    }

    public LiveData<Boolean> HasSelectedLanguage() {
        return _hasSelectedLanguage;
    }

    public LiveData<String> SelectedLanguage() { return _selectedLanguage; }

    public void Refresh() {
        Boolean has = _sharedPreferences.contains(getApplication().getString(R.string.PreferenceLanguageKey));

        _hasSelectedLanguage.setValue(has);
        _selectedLanguage.setValue(
                has ? _sharedPreferences.getString(getApplication().getString(R.string.PreferenceLanguageKey), "") :
                        "");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        Refresh();
    }
}