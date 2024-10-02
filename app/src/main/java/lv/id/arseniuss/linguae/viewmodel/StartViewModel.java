package lv.id.arseniuss.linguae.viewmodel;

import android.app.Application;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;

import androidx.annotation.NonNull;
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
import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.data.LanguageDataParser;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.UpdateDataAccess;

public class StartViewModel extends AndroidViewModel implements LanguageDataParser.ParserInterface {
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final LanguageDataParser _dataParser = new LanguageDataParser(this);

    private final String _preferenceLanguageKey = getApplication().getString(R.string.PreferenceLanguageKey);
    private final String _preferenceLanguageLocationKey =
            getApplication().getString(R.string.PreferenceLanguageLocationKey);
    private final String _preferencePortalsKey = getApplication().getString(R.string.PreferencePortalsKey);

    private final String _defaultPortals = getApplication().getString(R.string.DefaultLanguagePortal);

    private final MutableLiveData<List<LanguageDataParser.LanguagePortal>> _portals =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<LanguageViewModel>> _languages = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> _selectedPortal = new MutableLiveData<>(0);
    private final MutableLiveData<String> _messages = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> _canContinue = new MutableLiveData<>(false);

    private WarningInterface _warn;
    private String _text = "";
    private String _databaseVersion = "";
    private Callback _continue;
    private RequestConfirmCallback _requestUpdateConfirm;

    public StartViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<List<LanguageDataParser.LanguagePortal>> Portals() { return _portals; }

    public MutableLiveData<List<LanguageViewModel>> Languages() { return _languages; }

    public MutableLiveData<Integer> SelectedPortal() { return _selectedPortal; }

    public MutableLiveData<String> Messages() { return _messages; }

    public LiveData<Boolean> CanContinue() {
        return _canContinue;
    }

    public boolean HasSelectedLanguage() {
        return !_sharedPreferences.getString(_preferenceLanguageKey, "").trim().isEmpty();
    }

    public void Start(Callback continueCallback, RequestConfirmCallback requestUpdateConfirm) {
        _continue = continueCallback;
        _requestUpdateConfirm = requestUpdateConfirm;

        Gson gson = new Gson();

        if (!HasSelectedLanguage()) {
            String jsonPortals = _sharedPreferences.getString(_preferencePortalsKey, _defaultPortals);
            Type listType = new TypeToken<List<String>>() { }.getType();
            List<String> portals = gson.fromJson(jsonPortals, listType);

            Disposable d = Single.fromCallable(() -> _dataParser.ParsePortals(portals))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(value -> {
                        _portals.setValue(value);
                        loadLanguages();
                    }, error -> {
                        if (_warn != null) _warn.Warn(error.getMessage());
                    });
        }
        else {
            String language = _sharedPreferences.getString(_preferenceLanguageKey, "");

            UpdateDataAccess updateDataAccess =
                    LanguageDatabase.GetInstance(getApplication().getBaseContext(), language).GetUpdateDataAccess();

            Disposable d = updateDataAccess.GetVersion()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {
                        _databaseVersion = s;
                        parseLanguageFile();
                    }, error -> Inform(error.getMessage()), this::parseLanguageFile);

        }
    }

    private void parseLanguageFile() {
        String languageLocation = _sharedPreferences.getString(_preferenceLanguageLocationKey, "");
        Uri languageLocationUri = Uri.parse(languageLocation);

        Disposable d = Single.fromCallable(() -> _dataParser.Parse(languageLocationUri, false))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    if (success) validate();
                    _canContinue.postValue(true);
                });
    }

    private void validate() {
        if (_databaseVersion == null || _databaseVersion.isEmpty()) {
            updateDatabase(true);
        }
        else if (_databaseVersion.compareTo(_dataParser.GetData().LanguageVersion) < 0) {
            _requestUpdateConfirm.Request(this::updateDatabase);
        }
        else {
            if (BuildConfig.DEBUG) {
                updateDatabase(true);
            }
            else { _continue.Call(); }
        }
    }

    private void updateDatabase(Boolean confirm) {
        if (confirm) {
            String language = _sharedPreferences.getString(_preferenceLanguageKey, "");
            UpdateDataAccess updateDataAccess =
                    LanguageDatabase.GetInstance(getApplication().getBaseContext(), language).GetUpdateDataAccess();

            Disposable d = updateDataAccess.PerformUpdate(_dataParser.GetData())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        Inform("Saved"); // TODO: change this
                        _continue.Call();
                    });
        }
        else {
            _canContinue.setValue(true);
        }
    }

    private void loadLanguages() {
        _languages.setValue(new ArrayList<>());

        LanguageDataParser.LanguagePortal languagePortal =
                Objects.requireNonNull(_portals.getValue()).get(_selectedPortal.getValue());

        _languages.setValue(languagePortal.Languages.stream().map(LanguageViewModel::new).collect(Collectors.toList()));
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
    public void Inform(String message) {
        _text += message + "\n";

        _messages.postValue(_text);
    }

    public void SetWarning(WarningInterface o) {
        _warn = o;
    }

    public void SetSelectedLanguage(int position) {
        if (position >= 0) {
            LanguageViewModel languageViewModel = Objects.requireNonNull(_languages.getValue()).get(position);

            if (languageViewModel != null) {
                LanguageDataParser.LanguagePortal languagePortal =
                        Objects.requireNonNull(_portals.getValue()).get(_selectedPortal.getValue());

                _sharedPreferences.edit()
                        .putString(_preferenceLanguageKey, languageViewModel.Language)
                        .putString(_preferenceLanguageLocationKey, languageViewModel.Location)
                        .apply();
            }
        }

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
        public String Language;
        public String Image;
        public String Location;

        public LanguageViewModel(LanguageDataParser.Language l) {
            Language = l.Name;
            Image = l.Image;
            Location = l.Location;
        }
    }
}
