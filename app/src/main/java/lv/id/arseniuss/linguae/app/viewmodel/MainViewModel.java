package lv.id.arseniuss.linguae.app.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.preference.PreferenceManager;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.app.Configuration;
import lv.id.arseniuss.linguae.app.Constants;
import lv.id.arseniuss.linguae.app.Settings;
import lv.id.arseniuss.linguae.app.db.LanguageDatabase;
import lv.id.arseniuss.linguae.app.db.dataaccess.MainDataAccess;
import lv.id.arseniuss.linguae.app.db.entities.ConfigEntity;
import lv.id.arseniuss.linguae.app.db.entities.SettingEntity;

public class MainViewModel extends AndroidViewModel {
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _language =
            _sharedPreferences.getString(Constants.PreferenceLanguageCodeKey, "");
    private final MainDataAccess _mainDataAccess;
    private OnStartedListener _onStartedListener;

    public MainViewModel(@NonNull Application application) {
        super(application);

        if (!_language.isEmpty()) {
            _mainDataAccess =
                    LanguageDatabase.GetInstance(getApplication(), _language).GetMainDataAccess();
        } else {
            _mainDataAccess = null;
        }
    }

    public void Start(OnStartedListener onStartedListener) {
        _onStartedListener = onStartedListener;

        if (_mainDataAccess != null) {
            Disposable d = _mainDataAccess.GetSettings()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::gotSettings, this::handleError);
        }
    }

    private void handleError(Throwable error) {
        /* IGNORE */
    }

    private void gotSettings(List<SettingEntity> settingEntities) {
        Settings.Parse(settingEntities);

        Disposable d = _mainDataAccess.GetConfig()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::gotConfig);
    }

    private void gotConfig(List<ConfigEntity> configs) {
        Configuration.Parse(configs);

        if (_onStartedListener != null) _onStartedListener.Started();
    }

    public interface OnStartedListener {
        void Started();
    }
}
