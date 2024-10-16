package lv.id.arseniuss.linguae.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.preference.PreferenceManager;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.Configuration;
import lv.id.arseniuss.linguae.Constants;
import lv.id.arseniuss.linguae.Settings;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.MainDataAccess;
import lv.id.arseniuss.linguae.db.entities.Config;
import lv.id.arseniuss.linguae.db.entities.Setting;

public class MainViewModel extends AndroidViewModel {
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _language = _sharedPreferences.getString(Constants.PreferenceLanguageKey, "");
    private final MainDataAccess _mainDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetMainDataAccess();
    private OnStartedListener _onStartedListener;

    public MainViewModel(@NonNull Application application) {
        super(application);

    }

    public void Start(OnStartedListener onStartedListener) {
        _onStartedListener = onStartedListener;

        Disposable d = _mainDataAccess.GetSettings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::gotSettings);
    }

    private void gotSettings(List<Setting> settings) {
        Settings.Parse(settings);

        Disposable d = _mainDataAccess.GetConfig()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::gotConfig);
    }

    private void gotConfig(List<Config> configs) {
        Configuration.Parse(configs);

        if (_onStartedListener != null) _onStartedListener.Started();
    }

    public interface OnStartedListener {
        void Started();
    }
}
