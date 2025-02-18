package lv.id.arseniuss.linguae.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.Constants;
import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.Utilities;
import lv.id.arseniuss.linguae.data.ItemLanguageRepo;
import lv.id.arseniuss.linguae.data.LanguageDataParser;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.UpdateDataAccess;

public class StartViewModel extends AndroidViewModel implements LanguageDataParser.ParserInterface {

    public enum State {
        STATE_LOADING,
        STATE_DATA,
        STATE_ERROR,

        STATE_NODATA
    }

    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final LanguageDataParser _dataParser =
            new LanguageDataParser(this, _sharedPreferences
                    .getBoolean(Constants.PreferenceSaveImagesKey, false));

    private final String _defaultLanguageRepositories =
            getApplication().getString(R.string.DefaultLanguageRepositories);

    private final MutableLiveData<List<RepositoryViewModel>> _repositories =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<LanguageViewModel>> _languages =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> _selectedRepository = new MutableLiveData<>(0);
    private final MutableLiveData<Spanned> _messages =
            new MutableLiveData<>(new SpannableString(""));
    private final MutableLiveData<Boolean> _canContinue = new MutableLiveData<>(false);
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>("");
    private final MutableLiveData<Integer> _state =
            new MutableLiveData<>(State.STATE_LOADING.ordinal());
    private final MutableLiveData<String> _repositoryName = new MutableLiveData<>("");

    private final Gson gson = new Gson();
    Type listType = new TypeToken<List<ItemLanguageRepo>>() {
    }.getType();
    private WarningInterface _warn;
    private Spanned _text = new SpannableString("");
    private String _databaseVersion = "";
    private Callback _continue;
    private RequestConfirmCallback _requestUpdateConfirm;

    public StartViewModel(@NonNull Application application) {
        super(application);
        _selectedRepository.observeForever(o -> {
            loadRepository();
        });
    }

    public MutableLiveData<List<RepositoryViewModel>> Repositories() {
        return _repositories;
    }

    public MutableLiveData<Integer> State() {
        return _state;
    }

    public MutableLiveData<String> GetRepositoryName() {
        return _repositoryName;
    }

    public MutableLiveData<List<LanguageViewModel>> Languages() {
        return _languages;
    }

    public MutableLiveData<Integer> SelectedRepository() {
        return _selectedRepository;
    }

    public MutableLiveData<Spanned> Messages() {
        return _messages;
    }

    public LiveData<Boolean> CanContinue() {
        return _canContinue;
    }

    public MutableLiveData<String> ErrorMessage() {
        return _errorMessage;
    }

    public boolean HasSelectedLanguage() {
        return !_sharedPreferences.getString(Constants.PreferenceLanguageKey, "").trim().isEmpty();
    }

    public void StartRepositoryLoad(WarningInterface warn) {
        _warn = warn;

        List<ItemLanguageRepo> repos;
        String jsonRepoList = _sharedPreferences.getString(Constants.PreferenceRepositoriesKey, "");

        if (jsonRepoList.isEmpty()) {
            repos = new ArrayList<>();
        } else {
            repos = gson.fromJson(jsonRepoList, listType);
        }

        if (repos.isEmpty()) {
            for (String repo : _defaultLanguageRepositories.split(",")) {
                Uri uri = Uri.parse(repo);

                repos.add(new ItemLanguageRepo(uri.getHost(), repo));
            }

            saveRepositories(repos);
        }

        loadRepositories(repos);
    }

    public void StartLanguageParsing(boolean restart, Callback continueCallback,
                                     RequestConfirmCallback requestUpdateConfirm) {
        String language = _sharedPreferences.getString(Constants.PreferenceLanguageKey, "");
        String languageUrl = _sharedPreferences.getString(Constants.PreferenceLanguageUrlKey, "");

        StartLanguageParsing(restart, language, languageUrl, continueCallback,
                requestUpdateConfirm);
    }

    public void StartLanguageParsing(boolean restart, @Nullable String language,
                                     @Nullable String languageUrl,
                                     Callback continueCallback,
                                     RequestConfirmCallback requestUpdateConfirm) {
        _continue = continueCallback;
        _requestUpdateConfirm = requestUpdateConfirm;

        UpdateDataAccess updateDataAccess =
                LanguageDatabase.GetInstance(getApplication().getBaseContext(), language)
                        .GetUpdateDataAccess();

        Disposable d = updateDataAccess.GetVersion()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    Inform(Log.INFO, getApplication().getString(R.string.DatabaseVersion) + s);
                    _databaseVersion = s;
                    parseLanguageFile(restart, language, languageUrl);
                }, this::onError, () -> parseLanguageFile(restart, language, languageUrl));
    }

    private void loadRepositories(List<ItemLanguageRepo> repos) {
        Disposable d = Single.fromCallable(() -> _dataParser.ParseRepositories(
                        repos.stream()
                                .map(r -> r.Location)
                                .collect(Collectors.toList())))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((value, error) -> {
                    if (value == null) {
                        value = repos.stream()
                                .map(r -> new LanguageDataParser.LanguageRepository(r.Name,
                                        r.Location))
                                .collect(Collectors.toList());
                    }

                    List<RepositoryViewModel> repositoryViewModels =
                            IntStream.range(0, repos.size())
                                    .boxed()
                                    .collect(Collectors.toMap(repos::get, value::get))
                                    .entrySet()
                                    .stream()
                                    .map(e -> new RepositoryViewModel(e.getKey().Name,
                                            e.getValue()))
                                    .collect(Collectors.toList());

                    _repositories.setValue(repositoryViewModels);

                    if (error != null) {
                        warn(error.getMessage());
                    } else {
                        loadRepository();
                    }
                });
    }

    private void warn(String message) {
        _state.setValue(State.STATE_ERROR.ordinal());
        _errorMessage.postValue(message);
    }

    private void saveRepositories(List<ItemLanguageRepo> repos) {
        String jsonRepositories = gson.toJson(repos, listType);

        _sharedPreferences.edit()
                .putString(Constants.PreferenceRepositoriesKey, jsonRepositories)
                .apply();
    }

    private void parseLanguageFile(boolean restart, String language, String languageUrl) {
        Disposable d = Single.fromCallable(() -> _dataParser.ParseLanguageFile(languageUrl, false))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    if (success) {
                        validateVersions(restart, language, languageUrl);
                    } else {
                        _canContinue.postValue(true);
                    }
                }, this::onError);
    }

    private void validateVersions(boolean restart, String language, String languageUrl) {
        String repositoryVersion = _dataParser.GetData().LanguageVersion;

        Inform(Log.INFO,
                getApplication().getString(R.string.RepositoryVersion) + repositoryVersion);

        if (_databaseVersion == null || _databaseVersion.isEmpty() || restart) {
            parseRepository(language, languageUrl);
        } else if (_databaseVersion.compareTo(repositoryVersion) < 0) {
            _requestUpdateConfirm.Request(r -> onUpdateRequest(r, language, languageUrl));
        } else {
            Inform(Log.INFO, getApplication().getString(R.string.DatabaseUpdateNotRequired));
            updateLanguagePreferences(language, languageUrl);
            _continue.Call();
        }
    }

    private void onUpdateRequest(boolean doUpdate, String language, String languageUrl) {
        if (doUpdate) {
            parseRepository(language, languageUrl);
        } else {
            _canContinue.postValue(true);
        }
    }

    private void parseRepository(String language, String languageUrl) {

        Disposable d = Single.fromCallable(() -> _dataParser.ParseRepository(languageUrl, false))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    updateDatabase(success, language, languageUrl);
                }, this::onError);
    }

    private void updateDatabase(boolean parseSuccessful, String language, String languageUrl) {
        UpdateDataAccess updateDataAccess =
                LanguageDatabase.GetInstance(getApplication().getBaseContext(), language)
                        .GetUpdateDataAccess();

        Disposable d = updateDataAccess.PerformUpdate(_dataParser.GetData())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    updateLanguagePreferences(language, languageUrl);

                    Inform(Log.INFO, getApplication().getString(R.string.DataSaved));

                    if (parseSuccessful) {
                        _continue.Call();
                    } else {
                        _canContinue.postValue(true);
                    }
                }, this::onError);
    }

    private void updateLanguagePreferences(String language, String languageUrl) {
        _sharedPreferences.edit()
                .putString(Constants.PreferenceLanguageKey, language)
                .putString(Constants.PreferenceLanguageUrlKey, languageUrl)
                .apply();
    }

    private void loadRepository() {
        _state.setValue(State.STATE_LOADING.ordinal());
        _languages.setValue(new ArrayList<>());

        List<RepositoryViewModel> repositoriesValue = _repositories.getValue();
        Integer selectedRepositoryValue = _selectedRepository.getValue();

        assert repositoriesValue != null;
        assert selectedRepositoryValue != null;

        if (selectedRepositoryValue >= 0 && selectedRepositoryValue < repositoriesValue.size()) {
            RepositoryViewModel repositoryViewModel =
                    repositoriesValue.get(selectedRepositoryValue);

            if (repositoryViewModel.Repository.Error != null &&
                    !repositoryViewModel.Repository.Error.isEmpty()) {
                _errorMessage.setValue(repositoryViewModel.Repository.Error);
                _repositoryName.setValue("");
                _state.setValue(State.STATE_ERROR.ordinal());
            } else {
                _errorMessage.setValue("");
                _languages.setValue(
                        repositoryViewModel.Repository.Languages.stream()
                                .map(LanguageViewModel::new)
                                .collect(Collectors.toList()));
                _repositoryName.setValue(repositoryViewModel.Repository.Name);
                _state.setValue(State.STATE_DATA.ordinal());
            }
        }
    }

    @Override
    public InputStream GetFile(String filename) throws Exception {
        return Utilities.GetInputStream(getApplication(), filename);
    }

    @Override
    public void Inform(int type, String message) {

        if (_warn != null) {
            Completable.fromCallable(() -> {
                _warn.Warn(message);
                return true;
            }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
        } else {
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
    }

    public Pair<String, String> GetLanguage(int position) {
        Pair<String, String> res = new Pair<>(null, null);

        if (position >= 0) {
            LanguageViewModel languageViewModel =
                    Objects.requireNonNull(_languages.getValue()).get(position);

            res = new Pair<>(languageViewModel.Language, languageViewModel.LanguageUrl);
        }

        return res;
    }

    public String GetRepositoriesJson() {
        return _sharedPreferences.getString(Constants.PreferenceRepositoriesKey, "");
    }

    public void SetRepositoriesJson(String json) {
        List<ItemLanguageRepo> repos = gson.fromJson(json, listType);

        _state.setValue(State.STATE_LOADING.ordinal());

        _sharedPreferences.edit().putString(Constants.PreferenceRepositoriesKey, json).apply();

        loadRepositories(repos);
    }

    private void onError(Throwable error) {
        Inform(Log.INFO, error.getMessage());
        _canContinue.postValue(true);
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

    public interface WarningInterface {
        void Warn(String message);
    }

    public static class RepositoryViewModel extends BaseObservable {
        public LanguageDataParser.LanguageRepository Repository;

        public String Name;

        public RepositoryViewModel(String name, LanguageDataParser.LanguageRepository value) {
            Name = name;
            Repository = value;
        }
    }

    public static class LanguageViewModel extends BaseObservable {
        public final String Language;
        public final String ImageUrl;
        public final String LanguageUrl;

        public LanguageViewModel(LanguageDataParser.Language l) {
            Language = l.Name;
            ImageUrl = l.Image;
            LanguageUrl = l.Location;
        }
    }
}
