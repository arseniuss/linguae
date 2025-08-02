package lv.id.arseniuss.linguae.app.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.app.BugReportHelper;
import lv.id.arseniuss.linguae.app.Constants;
import lv.id.arseniuss.linguae.app.Utilities;
import lv.id.arseniuss.linguae.app.db.LanguageDatabase;
import lv.id.arseniuss.linguae.app.db.dataaccess.BugReportDataAccess;
import lv.id.arseniuss.linguae.app.db.entities.BugReportEntity;
import lv.id.arseniuss.linguae.app.db.enumerators.BugLocation;
import lv.id.arseniuss.linguae.app.entities.SystemReport;
import lv.id.arseniuss.linguae.app.enumerators.BugDataType;

public class BugReportViewModel extends AndroidViewModel {
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _language =
            _sharedPreferences.getString(Constants.PreferenceLanguageCodeKey, "");
    private final BugReportDataAccess _bugReportDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetBugReportDataAccess();

    private final MutableLiveData<String> _text = new MutableLiveData<>("");
    private final MutableLiveData<String> _data = new MutableLiveData<>("");
    private final MutableLiveData<String> _screenshot = new MutableLiveData<>("");
    private final Map<BugDataType, String> _collectedData = new HashMap<>();
    private BugLocation _bugLocation = BugLocation.UNKNOWN_LOCATION;

    public BugReportViewModel(@NonNull Application application) {
        super(application);
        init();
    }

    private void init() {
        SystemReport bug = BugReportHelper.GatherData(getApplication());
        Gson gson = Utilities.GetGsonBuilder().setPrettyPrinting().create();

        _collectedData.put(BugDataType.SYSTEM, gson.toJson(bug));
    }

    public void SetLocation(BugLocation location) {
        _bugLocation = location;

        if (_bugLocation == BugLocation.APPLICATION_BUG) {
            if (_collectedData.containsKey(BugDataType.SYSTEM)) {
                _data.setValue(_collectedData.get(BugDataType.SYSTEM));
            }
        } else if (_bugLocation == BugLocation.TASK_BUG) {
            if (_collectedData.containsKey(BugDataType.TASK)) {
                _data.setValue(_collectedData.get(BugDataType.TASK));
            }
        } else {
            _data.setValue("");
        }
    }

    public BugLocation GetLocation() {
        return _bugLocation;
    }

    public MutableLiveData<String> Text() {
        return _text;
    }

    public MutableLiveData<String> Data() {
        return _data;
    }

    public MutableLiveData<String> Screenshot() {
        return _screenshot;
    }

    public void Save(OnSaveListener listener) {
        BugReportEntity bugReportEntity = new BugReportEntity();

        bugReportEntity.Location = _bugLocation;
        bugReportEntity.Description = Objects.requireNonNull(_text.getValue());
        bugReportEntity.Data = Objects.requireNonNull(_data.getValue());

        Disposable d = _bugReportDataAccess.Save(bugReportEntity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::OnBugReportSaved, listener::OnBugReportError);
    }

    public void AddData(BugDataType bugDataType, String dataString) {
        _collectedData.put(bugDataType, dataString);

        if (bugDataType == BugDataType.SCREENSHOT) {
            _screenshot.setValue(dataString);
        }
    }

    public interface OnSaveListener {
        void OnBugReportSaved();

        void OnBugReportError(Throwable throwable);
    }
}
