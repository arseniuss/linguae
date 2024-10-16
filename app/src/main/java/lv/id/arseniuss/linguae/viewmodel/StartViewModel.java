package lv.id.arseniuss.linguae.viewmodel;

import android.app.Application;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.BuildConfig;
import lv.id.arseniuss.linguae.Constants;
import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.data.ItemLanguageRepo;
import lv.id.arseniuss.linguae.data.LanguageDataParser;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.UpdateDataAccess;

public class StartViewModel extends AndroidViewModel implements LanguageDataParser.ParserInterface {
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final LanguageDataParser _dataParser = new LanguageDataParser(this);

    private final String _defaultPortals = getApplication().getString(R.string.DefaultLanguagePortal);

    private final MutableLiveData<List<LanguageDataParser.LanguagePortal>> _portals =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<LanguageViewModel>> _languages = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> _selectedPortal = new MutableLiveData<>(0);
    private final MutableLiveData<Spanned> _messages = new MutableLiveData<>(new SpannableString(""));
    private final MutableLiveData<Boolean> _canContinue = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _hasError = new MutableLiveData<>(false);
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>("");

    private final Gson gson = new Gson();
    Type listType = new TypeToken<List<ItemLanguageRepo>>() { }.getType();
    private WarningInterface _warn;
    private Spanned _text = new SpannableString("");
    private String _databaseVersion = "";
    private Callback _continue;
    private RequestConfirmCallback _requestUpdateConfirm;

    public StartViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<List<LanguageDataParser.LanguagePortal>> Portals() { return _portals; }

    public MutableLiveData<List<LanguageViewModel>> Languages() { return _languages; }

    public MutableLiveData<Integer> SelectedPortal() { return _selectedPortal; }

    public MutableLiveData<Spanned> Messages() { return _messages; }

    public LiveData<Boolean> CanContinue() {
        return _canContinue;
    }

    public MutableLiveData<Boolean> HasError() { return _hasError; }

    public MutableLiveData<String> ErrorMessage() { return _errorMessage; }

    public boolean HasSelectedLanguage() {
        return !_sharedPreferences.getString(Constants.PreferenceLanguageKey, "").trim().isEmpty();
    }

    public void StartPortalLoading() {
        List<ItemLanguageRepo> portals;
        String jsonPortals = _sharedPreferences.getString(Constants.PreferencePortalsKey, "");

        if (jsonPortals.isEmpty()) {
            portals = new ArrayList<>();
        }
        else {
            portals = gson.fromJson(jsonPortals, listType);
        }

        if (portals.isEmpty()) {
            portals.add(new ItemLanguageRepo(getApplication().getString(R.string.UnnamedRepository), _defaultPortals));
        }

        savePortals(portals);

        reloadPortals(portals);

    }

    public void StartLanguageParsing(Callback continueCallback, RequestConfirmCallback requestUpdateConfirm) {
        String language = _sharedPreferences.getString(Constants.PreferenceLanguageKey, "");
        String languageUrl = _sharedPreferences.getString(Constants.PreferenceLanguageUrlKey, "");

        StartLanguageParsing(language, languageUrl, continueCallback, requestUpdateConfirm);
    }

    public void StartLanguageParsing(@Nullable String language, @Nullable String languageUrl, Callback continueCallback,
            RequestConfirmCallback requestUpdateConfirm)
    {
        _continue = continueCallback;
        _requestUpdateConfirm = requestUpdateConfirm;

        UpdateDataAccess updateDataAccess =
                LanguageDatabase.GetInstance(getApplication().getBaseContext(), language).GetUpdateDataAccess();

        String finalLanguageUrl = languageUrl;
        String finalLanguage = language;

        Disposable d = updateDataAccess.GetVersion()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    Inform(Log.INFO, getApplication().getString(R.string.DatabaseVersion) + s);
                    _databaseVersion = s;
                    parseLanguageFile(finalLanguage, finalLanguageUrl);
                }, this::onError, () -> parseLanguageFile(finalLanguage, finalLanguageUrl));
    }

    private void reloadPortals(List<ItemLanguageRepo> repos) {
        Disposable d = Single.fromCallable(() -> _dataParser.ParsePortals(
                        repos.stream().map(r -> new Pair<>(r.Name, r.Location)).collect(Collectors.toList())))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((value, error) -> {
                    if (value == null) {
                        value = repos.stream()
                                .map(r -> new LanguageDataParser.LanguagePortal(r.Name, r.Location))
                                .collect(Collectors.toList());
                    }
                    savePortals(value.stream()
                            .map(v -> new ItemLanguageRepo(v.Name, v.Location))
                            .collect(Collectors.toList()));
                    _portals.setValue(value);
                    if (error != null) { warn(error.getMessage()); }
                    else { loadLanguages(); }
                });
    }

    private void warn(String message) {
        _hasError.postValue(true);
        _errorMessage.postValue(message);
    }

    private void savePortals(List<ItemLanguageRepo> portals) {
        String jsonPortals = gson.toJson(portals, listType);
        _sharedPreferences.edit().putString(Constants.PreferencePortalsKey, jsonPortals).apply();
    }

    private void parseLanguageFile(String language, String languageUrl) {
        Uri languageLocationUri = Uri.parse(languageUrl);

        Disposable d = Single.fromCallable(() -> _dataParser.ParseLanguageFile(languageLocationUri, false))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    if (success) { validateVersions(language, languageUrl); }
                    else { _canContinue.postValue(true); }
                }, this::onError);
    }

    private void validateVersions(String language, String languageUrl) {
        String repositoryVersion = _dataParser.GetData().LanguageVersion;

        Inform(Log.INFO, getApplication().getString(R.string.RepositoryVersion) + repositoryVersion);

        if (_databaseVersion == null || _databaseVersion.isEmpty()) {
            parseRepository(language, languageUrl);
        }
        else if (_databaseVersion.compareTo(repositoryVersion) < 0) {
            _requestUpdateConfirm.Request(r -> onUpdateRequest(r, language, languageUrl));
        }
        else {
            Inform(Log.INFO, getApplication().getString(R.string.DatabaseUpdateNotRequired));
            updateLanguagePreferences(language, languageUrl);
            _continue.Call();
        }
    }

    private void onUpdateRequest(boolean doUpdate, String language, String languageUrl) {
        if (doUpdate) {
            parseRepository(language, languageUrl);
        }
        else {
            _canContinue.postValue(true);
        }
    }

    private void parseRepository(String language, String languageUrl) {
        Uri languageLocationUri = Uri.parse(languageUrl);

        Disposable d = Single.fromCallable(() -> _dataParser.ParseRepository(languageLocationUri, false))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    updateDatabase(success, language, languageUrl);
                }, this::onError);
    }

    private void updateDatabase(boolean parseSuccessful, String language, String languageUrl) {
        UpdateDataAccess updateDataAccess =
                LanguageDatabase.GetInstance(getApplication().getBaseContext(), language).GetUpdateDataAccess();

        Disposable d = updateDataAccess.PerformUpdate(_dataParser.GetData())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    updateLanguagePreferences(language, languageUrl);
                    Inform(Log.INFO, getApplication().getString(R.string.DataSaved));
                    if (parseSuccessful) { _continue.Call(); }
                    else { _canContinue.postValue(true); }
                }, this::onError);
    }

    private void updateLanguagePreferences(String language, String languageUrl) {
        _sharedPreferences.edit()
                .putString(Constants.PreferenceLanguageKey, language)
                .putString(Constants.PreferenceLanguageUrlKey, languageUrl)
                .apply();
    }

    private void loadLanguages() {
        _languages.setValue(new ArrayList<>());

        List<LanguageDataParser.LanguagePortal> portals = _portals.getValue();
        Integer selectedPortalValue = _selectedPortal.getValue();

        assert portals != null;
        assert selectedPortalValue != null;

        if (selectedPortalValue >= 0 && selectedPortalValue < portals.size()) {
            LanguageDataParser.LanguagePortal languagePortal = portals.get(selectedPortalValue);

            _languages.setValue(
                    languagePortal.Languages.stream().map(LanguageViewModel::new).collect(Collectors.toList()));
        }
    }

    private Uri getDocument(Uri base, String filename) {
        ContentResolver resolver = getApplication().getContentResolver();
        Uri childrenUri =
                DocumentsContract.buildChildDocumentsUriUsingTree(base, DocumentsContract.getTreeDocumentId(base));
        Cursor cursor = resolver.query(childrenUri, new String[]{
                DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
        }, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String documentId = cursor.getString(0);
                String displayName = cursor.getString(1);
                String mimeType = cursor.getString(2);

                if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType)) {
                    Uri newTreeUri = DocumentsContract.buildChildDocumentsUriUsingTree(base, documentId);

                    // TODO: we shoudn't go deeper

                    return getDocument(newTreeUri, filename);
                }
                else {
                    if (displayName.equals(filename)) {
                        return DocumentsContract.buildDocumentUriUsingTree(base, documentId);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public InputStream GetFile(Uri base, String filename) throws IOException {
        String scheme = base.getScheme();

        if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            // Handle content URI
            ContentResolver resolver = getApplication().getContentResolver();
            Uri documentUri = getDocument(base, filename);

            if (documentUri == null) throw new NullPointerException();

            return resolver.openInputStream(documentUri);
        }
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            // Handle file URI
            String path = base + "/" + filename;

            return new FileInputStream(new File(path));
        }
        else if ("http".equals(scheme) || "https".equals(scheme)) {
            // Handle HTTP/HTTPS URL
            String path = base + "/" + filename;
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            return connection.getInputStream();
        }
        else {
            throw new IllegalArgumentException("Unsupported URI scheme: " + scheme);
        }
    }

    @Override
    public void Inform(int type, String message) {
        Spanned spanned;

        switch (type) {
            case Log.ERROR:
                if (BuildConfig.DEBUG) Log.i("INFORM", message);
                spanned = Html.fromHtml("<font color='#FF0000'>" + message + "</font>", Html.FROM_HTML_MODE_LEGACY);
                break;
            case Log.INFO:
                if (BuildConfig.DEBUG) Log.i("INFORM", message);
            default:
                spanned = Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY);
                break;
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        spannableStringBuilder.append(_text);
        spannableStringBuilder.append(Html.fromHtml("<br>", Html.FROM_HTML_MODE_LEGACY));
        spannableStringBuilder.append(spanned);

        _text = spannableStringBuilder;

        _messages.postValue(_text);
    }

    public Pair<String, String> GetLanguage(int position) {
        Pair<String, String> res = new Pair<>(null, null);

        if (position >= 0) {
            LanguageViewModel languageViewModel = Objects.requireNonNull(_languages.getValue()).get(position);

            res = new Pair<>(languageViewModel.Language, languageViewModel.LanguageUrl);
        }

        return res;
    }

    public String GetPortalsJson() {
        return _sharedPreferences.getString(Constants.PreferencePortalsKey, "");
    }

    public void SetPortalsJson(String json) {
        List<ItemLanguageRepo> portals = gson.fromJson(json, listType);

        _sharedPreferences.edit().putString(Constants.PreferencePortalsKey, json).apply();
        reloadPortals(portals);
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

    public static class LanguageViewModel extends BaseObservable {
        public final String Language;
        public final String Image;
        public final String LanguageUrl;

        public LanguageViewModel(LanguageDataParser.Language l) {
            Language = l.Name;
            Image = l.Image;
            LanguageUrl = l.Location;
        }
    }
}
