package lv.id.arseniuss.linguae.viewmodel;

import android.app.Application;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;

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

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.BuildConfig;
import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.data.ItemLanguageRepo;
import lv.id.arseniuss.linguae.data.LanguageDataParser;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.UpdateDataAccess;

public class FirstViewModel extends AndroidViewModel implements LanguageDataParser.ParserInterface {
    private final SharedPreferences _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            getApplication().getBaseContext());
    private final MutableLiveData<String> _messages = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> _canContinue = new MutableLiveData<>(false);
    private final String _preferenceLanguage = getApplication().getString(R.string.PreferenceLanguageKey);
    private final String _preferenceLanguageList = getApplication().getString(R.string.PreferenceLanguageListKey);
    private final String _preferenceLanguageLocation = getApplication().getString(
            R.string.PreferenceLanguageLocationKey);
    private final LanguageDataParser _dataParser = new LanguageDataParser(this);
    private String _text = "";
    private String _databaseVersion = "";
    private Callback _continue;
    private RequestConfirmCallback _requestUpdateConfirm;

    public FirstViewModel(Application app) {
        super(app);
    }

    public LiveData<String> Messages() { return _messages; }

    public LiveData<Boolean> CanContinue() {
        return _canContinue;
    }

    private void checkLanguageRepos() {
        final String defaultLanguage = getApplication().getString(R.string.DefaultLanguageName);
        final String defaultLanguageLocation = getApplication().getString(R.string.DefaultLanguageLocation);

        String languageJson = _sharedPreferences.getString(_preferenceLanguageList, "");
        Type listOfMyClassObject = new TypeToken<ArrayList<ItemLanguageRepo>>() { }.getType();
        List<ItemLanguageRepo> repos = new Gson().fromJson(languageJson, listOfMyClassObject);

        if (repos == null) repos = new ArrayList<>();

        if (repos.isEmpty()) {
            repos.add(new ItemLanguageRepo(defaultLanguage, defaultLanguageLocation));

            languageJson = new Gson().toJson(repos, listOfMyClassObject);

            _sharedPreferences.edit().putString(_preferenceLanguageList, languageJson).apply();
        }

        String language = _sharedPreferences.getString(_preferenceLanguage, "");
        String languageLocation = _sharedPreferences.getString(_preferenceLanguageLocation, "");

        if (language.isEmpty() && languageLocation.isEmpty()) {
            _sharedPreferences.edit()
                    .putString(_preferenceLanguage, defaultLanguage)
                    .putString(_preferenceLanguageLocation, defaultLanguageLocation)
                    .apply();
        }
    }

    public void Start(Callback continueCallback, RequestConfirmCallback requestUpdateConfirm) {
        _continue = continueCallback;
        _requestUpdateConfirm = requestUpdateConfirm;
        checkLanguageRepos();

        String language = _sharedPreferences.getString(_preferenceLanguage, "");
        String languageLocation = _sharedPreferences.getString(_preferenceLanguageLocation, "");

        if (!language.isEmpty() && !languageLocation.isEmpty()) {
            UpdateDataAccess updateDataAccess = LanguageDatabase.GetInstance(getApplication().getBaseContext(),
                    language).GetUpdateDataAccess();

            Disposable d = updateDataAccess.GetVersion()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {
                        _databaseVersion = s;
                        parseLanguageFile();
                    }, throwable -> { _messages.postValue(throwable.getMessage()); }, this::parseLanguageFile);
        }
        else {
            _continue.Call();
        }
    }

    private void parseLanguageFile() {
        String languageLocation = _sharedPreferences.getString(_preferenceLanguageLocation, "");
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
            String language = _sharedPreferences.getString(_preferenceLanguage, "");
            UpdateDataAccess updateDataAccess = LanguageDatabase.GetInstance(getApplication().getBaseContext(),
                    language).GetUpdateDataAccess();


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

    private Uri getDocument(Uri base, String filename) {
        ContentResolver resolver = getApplication().getContentResolver();
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(base,
                DocumentsContract.getTreeDocumentId(base));
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

    public interface Callback {
        void Call();
    }

    public interface RequestConfirmCallback {
        void Request(ConfirmResponseCallback callback);
    }

    public interface ConfirmResponseCallback {
        void ConfirmResponse(boolean confirmed);
    }

}
