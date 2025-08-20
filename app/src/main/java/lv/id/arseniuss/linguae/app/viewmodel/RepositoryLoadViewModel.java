package lv.id.arseniuss.linguae.app.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import java.io.InputStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.app.Constants;
import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.Utilities;
import lv.id.arseniuss.linguae.app.db.LanguageDatabase;
import lv.id.arseniuss.linguae.app.db.dataaccess.UpdateDataAccess;
import lv.id.arseniuss.linguae.parsers.LanguageDataParser;

public class RepositoryLoadViewModel extends AndroidViewModel
        implements LanguageDataParser.ParserInterface {
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final LanguageDataParser _dataParser =
            new LanguageDataParser(this, Utilities.GetLanguageCodes(_sharedPreferences),
                    _sharedPreferences.getBoolean(Constants.PreferenceSaveImagesKey, false));
    private final MutableLiveData<Spanned> _messages =
            new MutableLiveData<>(new SpannableString(""));

    private final MutableLiveData<Boolean> _canContinue = new MutableLiveData<>(false);
    private Callback _continue;
    private RequestConfirmCallback _requestUpdateConfirm;

    private String _languageUrl;
    private String _databaseVersion = null;
    private Spanned _text = new SpannableString("");
    private Boolean _restart = false;

    public RepositoryLoadViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<Spanned> Messages() {
        return _messages;
    }

    public LiveData<Boolean> CanContinue() {
        return _canContinue;
    }

    public void Load(Callback continueCallback, RequestConfirmCallback requestUpdateConfirm,
                     Boolean restart) {
        _continue = continueCallback;
        _requestUpdateConfirm = requestUpdateConfirm;
        _restart = restart;

        final String languageCode =
                _sharedPreferences.getString(Constants.PreferenceLanguageCodeKey, "");
        _languageUrl = _sharedPreferences.getString(Constants.PreferenceLanguageUrlKey, "");

        UpdateDataAccess updateDataAccess =
                LanguageDatabase.GetInstance(getApplication().getBaseContext(), languageCode)
                        .GetUpdateDataAccess();

        Disposable d = updateDataAccess.GetVersion()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onGotVersion, this::onError, this::onDoneVersionGetting);
    }

    private void onDoneVersionGetting() {
        parseLanguage();
    }

    private void onGotVersion(String versionString) {
        _databaseVersion = versionString;
        parseLanguage();
    }

    private void parseLanguage() {
        Disposable d = Single.fromCallable(() -> _dataParser.ParseLanguageFile(_languageUrl, false))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onLanguageFileResult, this::onError);
    }

    private void onLanguageFileResult(Boolean success) {
        if (success) validateVersions();
        else _canContinue.postValue(true);
    }

    private void validateVersions() {
        String repositoryVersion = _dataParser.GetData().LanguageVersion;

        Inform(Log.INFO,
                getApplication().getString(R.string.RepositoryVersion) + repositoryVersion);

        if (_databaseVersion == null || _databaseVersion.isEmpty() || _restart) {
            parseRepository();
        } else if (_databaseVersion.compareTo(repositoryVersion) < 0) {
            _requestUpdateConfirm.Request(this::onUpdateRequest);
        } else {
            Inform(Log.INFO, getApplication().getString(R.string.DatabaseUpdateNotRequired));
            updateLanguagePreferences();
            _continue.Call();
        }
    }

    private void onUpdateRequest(boolean doUpdate) {
        if (doUpdate) {
            parseRepository();
        } else {
            _canContinue.postValue(true);
        }
    }

    private void parseRepository() {
        Disposable d = Single.fromCallable(() -> _dataParser.ParseRepository(_languageUrl, false))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateDatabase, this::onError);
    }

    private void updateDatabase(boolean parseSuccessful) {
        if (!parseSuccessful) {
            _canContinue.postValue(true);
            return;
        }

        final String languageCode =
                _sharedPreferences.getString(Constants.PreferenceLanguageCodeKey, "");

        UpdateDataAccess updateDataAccess =
                LanguageDatabase.GetInstance(getApplication().getBaseContext(), languageCode)
                        .GetUpdateDataAccess();

        Disposable d = updateDataAccess.PerformUpdate(_dataParser.GetData())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    updateLanguagePreferences();

                    Inform(Log.INFO, getApplication().getString(R.string.DataSaved));

                    if (parseSuccessful) {
                        _continue.Call();
                    } else {
                        _canContinue.postValue(true);
                    }
                }, this::onError);
    }

    private void updateLanguagePreferences() {
        LanguageDataParser.ParserData parserData = _dataParser.GetData();

        _sharedPreferences.edit()
                .putString(Constants.PreferenceLanguageCodeKey, parserData.LanguageCode)
                .putString(Constants.PreferenceLanguageNameKey, parserData.LanguageName)
                .putString(Constants.PreferenceLanguageUrlKey, parserData.LanguageUrl)
                .apply();
    }

    @Override
    public InputStream GetFile(String filename) throws Exception {
        return Utilities.GetInputStream(getApplication().getBaseContext(), filename);
    }

    private void onError(Throwable error) {
        Inform(Log.INFO, error.getMessage());
        _canContinue.postValue(true);
    }

    @Override
    public void Inform(int type, String message) {
        Spanned spanned;
        String msg = Html.escapeHtml(message);

        switch (type) {
            case Log.ERROR:
                Log.e("INFORM", message);
                spanned = Html.fromHtml("<font color='#FF0000'>" + msg + "</font>",
                        Html.FROM_HTML_MODE_LEGACY);
                break;
            case Log.INFO:
                Log.i("INFORM", message);
            default:
                spanned = Html.fromHtml(msg, Html.FROM_HTML_MODE_LEGACY);
                break;
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        spannableStringBuilder.append(_text);
        spannableStringBuilder.append(Html.fromHtml("<br>", Html.FROM_HTML_MODE_LEGACY));
        spannableStringBuilder.append(spanned);

        _text = spannableStringBuilder;

        _messages.postValue(_text);
    }

    public interface ConfirmResponseCallback {
        void ConfirmResponse(boolean confirmed);
    }

    public interface RequestConfirmCallback {
        void Request(ConfirmResponseCallback callback);
    }

    public interface Callback {
        void Call();
    }
}
